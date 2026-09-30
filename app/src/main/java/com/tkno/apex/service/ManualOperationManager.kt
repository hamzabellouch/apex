package com.tkno.apex.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.tkno.apex.util.AppStorageHelper

object ManualOperationManager {

    var isRunning = false
        private set

    var currentMode = ServiceMode.FORCE_STOP
        private set

    var currentPackage: String? = null
        private set

    var currentAppIndex = 0
        private set

    var totalAppsCount = 0
        private set

    var progressCallback: ((index: Int, total: Int, packageName: String) -> Unit)? = null
    var itemResultCallback: ((packageName: String, success: Boolean, mode: ServiceMode) -> Unit)? = null
    var completionCallback: (() -> Unit)? = null

    private var packageList = listOf<String>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private var isTransitioning = false
    private var initialAppCacheBytes = 0L

    fun start(context: Context, packages: List<String>, mode: ServiceMode) {
        if (packages.isEmpty()) return
        stop(context, returnToApp = false)

        isRunning = true
        currentMode = mode
        packageList = packages
        currentAppIndex = 0
        totalAppsCount = packages.size
        isTransitioning = false

        processCurrentOrNextApp(context.applicationContext)
    }

    private fun processCurrentOrNextApp(context: Context) {
        if (!isRunning) return

        while (currentAppIndex < packageList.size) {
            val pkg = packageList[currentAppIndex]

            // Fast-path: Check if already stopped or cache is already 0
            if (currentMode == ServiceMode.FORCE_STOP) {
                if (AppStorageHelper.isAppStopped(context, pkg)) {
                    progressCallback?.invoke(currentAppIndex, packageList.size, pkg)
                    itemResultCallback?.invoke(pkg, true, ServiceMode.FORCE_STOP)
                    currentAppIndex++
                    continue
                }
            } else {
                val liveCache = AppStorageHelper.getSingleAppCacheBytes(context, pkg)
                if (liveCache <= 0L) {
                    progressCallback?.invoke(currentAppIndex, packageList.size, pkg)
                    itemResultCallback?.invoke(pkg, true, ServiceMode.CLEAR_CACHE)
                    currentAppIndex++
                    continue
                }
            }

            // Target app needs manual user action
            currentPackage = pkg
            initialAppCacheBytes = if (currentMode == ServiceMode.CLEAR_CACHE) {
                AppStorageHelper.getSingleAppCacheBytes(context, pkg)
            } else 0L

            progressCallback?.invoke(currentAppIndex, packageList.size, pkg)
            openAppDetailsSettings(context, pkg)
            startPolling(context, pkg)
            return
        }

        // All packages processed -> finish & return to app
        stop(context, returnToApp = true)
    }

    private fun startPolling(context: Context, targetPkg: String) {
        stopPolling()
        isTransitioning = false

        pollingRunnable = object : Runnable {
            override fun run() {
                if (!isRunning || isTransitioning || currentPackage != targetPkg) return

                val isActionDone = if (currentMode == ServiceMode.FORCE_STOP) {
                    AppStorageHelper.isAppStopped(context, targetPkg)
                } else {
                    val currentCache = AppStorageHelper.getSingleAppCacheBytes(context, targetPkg)
                    currentCache == 0L || (initialAppCacheBytes > 0L && currentCache == 0L)
                }

                if (isActionDone) {
                    isTransitioning = true
                    itemResultCallback?.invoke(targetPkg, true, currentMode)

                    // Small delay to allow the OS to finish closing the app / clearing cache smoothly
                    mainHandler.postDelayed({
                        if (!isRunning) return@postDelayed
                        currentAppIndex++
                        processCurrentOrNextApp(context)
                    }, 250L)
                    return
                }

                // Poll every 250ms
                mainHandler.postDelayed(this, 250L)
            }
        }
        mainHandler.postDelayed(pollingRunnable!!, 350L)
    }

    private fun stopPolling() {
        pollingRunnable?.let { mainHandler.removeCallbacks(it) }
        pollingRunnable = null
    }

    fun skipCurrentApp(context: Context) {
        if (!isRunning) return
        stopPolling()
        isTransitioning = false
        currentAppIndex++
        processCurrentOrNextApp(context.applicationContext)
    }

    fun retryCurrentApp(context: Context) {
        if (!isRunning) return
        val pkg = currentPackage ?: return
        openAppDetailsSettings(context, pkg)
        startPolling(context.applicationContext, pkg)
    }

    fun stop(context: Context? = null, returnToApp: Boolean = false) {
        if (!isRunning && pollingRunnable == null) return
        isRunning = false
        stopPolling()
        isTransitioning = false
        currentPackage = null

        val cb = completionCallback
        progressCallback = null
        itemResultCallback = null
        completionCallback = null

        if (returnToApp && context != null) {
            bringAppToForeground(context)
        }

        AppStorageHelper.clearAllMemoryCaches()
        cb?.invoke()
    }

    fun openAppDetailsSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bringAppToForeground(context: Context) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
