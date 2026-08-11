package com.tkno.apex.ui.page

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.tkno.apex.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Background composable that checks for updates on launch (if auto-update is enabled).
 * When a new version is found, it sends a system notification instead of immediately
 * blocking the screen with a popup. Tapping the notification opens the update dialog.
 */
@Composable
fun AppUpdater(isAutoUpdateEnabled: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("apex_prefs", Context.MODE_PRIVATE) }
    val updateChannel = prefs.getInt("update_channel", 1) // 1: Preview, 0: Stable
    val includePrerelease = updateChannel == 1
    val bellEnabled = prefs.getBoolean("update_bell_enabled", true)

    var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
    var currentDownloadStatus by remember {
        mutableStateOf<UpdateUtil.DownloadStatus>(UpdateUtil.DownloadStatus.NotYet)
    }
    var updateJob by remember { mutableStateOf<Job?>(null) }
    var release by remember { mutableStateOf(UpdateUtil.Release()) }

    // Check if Activity was launched from an update notification click
    LaunchedEffect(context) {
        val activity = context as? Activity
        val intent = activity?.intent
        if (intent != null && (intent.getBooleanExtra("show_update_dialog", false) || intent.action == "ACTION_SHOW_UPDATE_DIALOG")) {
            val name = intent.getStringExtra("release_name")
            val body = intent.getStringExtra("release_body")
            val tag = intent.getStringExtra("release_tag")
            if (!name.isNullOrBlank() || !tag.isNullOrBlank()) {
                release = UpdateUtil.Release(
                    name = name,
                    body = body,
                    tagName = tag
                )
                showUpdateDialog = true
                intent.removeExtra("show_update_dialog")
            }
        }
    }

    LaunchedEffect(isAutoUpdateEnabled, updateChannel) {
        if (!isAutoUpdateEnabled) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                UpdateUtil.checkForUpdate(context, includePrerelease = includePrerelease)?.let { foundRelease ->
                    release = foundRelease
                    if (bellEnabled) {
                        // Send System Notification instead of popping up dialog immediately
                        UpdateUtil.showUpdateNotification(context, foundRelease)
                    } else {
                        showUpdateDialog = true
                    }
                }
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                e.printStackTrace()
            }
        }
    }

    if (showUpdateDialog) {
        UpdateDialogImpl(
            onDismissRequest = {
                showUpdateDialog = false
                updateJob?.cancel()
            },
            title = release.name ?: release.tagName ?: "New Update",
            releaseNote = release.body ?: "",
            downloadStatus = currentDownloadStatus,
            onConfirmUpdate = {
                updateJob = scope.launch(Dispatchers.IO) {
                    runCatching {
                        UpdateUtil.downloadApk(context, release).collect { status ->
                            currentDownloadStatus = status
                            if (status is UpdateUtil.DownloadStatus.Finished) {
                                withContext(Dispatchers.Main) {
                                    UpdateUtil.installLatestApk(context)
                                }
                            }
                        }
                    }.onFailure {
                        it.printStackTrace()
                        currentDownloadStatus = UpdateUtil.DownloadStatus.NotYet
                    }
                }
            },
        )
    }
}
