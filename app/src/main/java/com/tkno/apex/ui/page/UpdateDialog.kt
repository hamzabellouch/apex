package com.tkno.apex.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tkno.apex.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UpdateDialog(
    onDismissRequest: () -> Unit,
    release: UpdateUtil.Release,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentDownloadStatus by remember {
        mutableStateOf<UpdateUtil.DownloadStatus>(UpdateUtil.DownloadStatus.NotYet)
    }

    val prefs = remember { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }
    val autoInstall = prefs.getBoolean("auto_install_apk", true)
    val canInstall = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else true
    val isAutoInstallActive = autoInstall && canInstall

    AlertDialog(
        onDismissRequest = {},
        title = { Text(release.name ?: "New Update Available") },
        icon = { Icon(Icons.Outlined.NewReleases, contentDescription = null) },
        confirmButton = {
            when (val status = currentDownloadStatus) {
                is UpdateUtil.DownloadStatus.Progress -> {
                    // إخفاء زر التأكيد أثناء التحميل
                }
                is UpdateUtil.DownloadStatus.Finished -> {
                    Button(onClick = {
                        if (isAutoInstallActive) {
                            UpdateUtil.installLatestApk(context)
                        } else {
                            UpdateUtil.openApkLocation(context, release)
                        }
                        onDismissRequest()
                    }) {
                        Text(if (isAutoInstallActive) "Install" else "Open in Files")
                    }
                }
                else -> {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            runCatching {
                                UpdateUtil.downloadApk(context, release).collect { st ->
                                    currentDownloadStatus = st
                                    if (st is UpdateUtil.DownloadStatus.Finished) {
                                        withContext(Dispatchers.Main) {
                                            if (isAutoInstallActive) {
                                                UpdateUtil.installLatestApk(context)
                                            } else {
                                                UpdateUtil.openApkLocation(context, release)
                                            }
                                        }
                                    }
                                }
                            }.onFailure {
                                it.printStackTrace()
                                currentDownloadStatus = UpdateUtil.DownloadStatus.NotYet
                            }
                        }
                    }) {
                        Text("Update Now")
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(if (currentDownloadStatus is UpdateUtil.DownloadStatus.Progress) "Hide" else "Cancel")
            }
        },
        text = {
            Column {
                when (val status = currentDownloadStatus) {
                    is UpdateUtil.DownloadStatus.Progress -> {
                        Column {
                            LinearProgressIndicator(
                                progress = { status.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                strokeCap = StrokeCap.Round,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Downloading update...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${status.percent}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    is UpdateUtil.DownloadStatus.Finished -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Downloaded Successfully!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            Text(
                                text = if (isAutoInstallActive) "Launching installer..." else "Saved to Downloads.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    else -> {
                        val notes = release.body ?: ""
                        if (notes.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}
