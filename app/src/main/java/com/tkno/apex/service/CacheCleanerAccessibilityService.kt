package com.tkno.apex.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.tkno.apex.util.AppStorageHelper

enum class ServiceMode {
    CLEAR_CACHE,
    FORCE_STOP
}

class CacheCleanerAccessibilityService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var watchdogRunnable: Runnable? = null
    private var periodicScanRunnable: Runnable? = null

    private var windowManager: WindowManager? = null

    // State tracking for current package execution
    private var hasClickedStorage = false
    private var hasClickedClearData = false
    private var hasClickedClearCache = false
    private var hasClickedConfirmClearCache = false
    private var hasClickedForceStop = false
    private var hasClickedConfirmForceStop = false
    private var scrollCountForStorage = 0
    private var scrollCountForClearCache = 0
    private var isStepInProgress = false
    private var pageOpenedTime = 0L
    private var lastAppOpenTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCleaning()
        AppStorageHelper.clearAllMemoryCaches()
        instance = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopCleaning()
        AppStorageHelper.clearAllMemoryCaches()
        android.os.Process.killProcess(android.os.Process.myUid())
        kotlin.system.exitProcess(0)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            AppStorageHelper.clearAllMemoryCaches()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        AppStorageHelper.clearAllMemoryCaches()
    }

    private fun bringAppToForeground() {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isSettingsPackage(packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        val lower = packageName.lowercase()
        return lower.contains("settings") ||
               lower.contains("securitycenter") ||
               lower.contains("systemmanager") ||
               lower.contains("safecenter") ||
               lower.contains("secure") ||
               lower.contains("controlcenter") ||
               lower.contains("permissioncontroller") ||
               lower.contains("packageinstaller") ||
               lower.contains("systemui") ||
               lower.contains("miui") ||
               lower.contains("samsung") ||
               lower.contains("huawei") ||
               lower.contains("oppo") ||
               lower.contains("vivo") ||
               lower.contains("coloros") ||
               lower.contains("transsion") ||
               lower.contains("oneplus") ||
               lower.contains("realme") ||
               lower.contains("honor") ||
               lower.contains("nothing") ||
               lower.contains("aliyun") ||
               lower.contains("meizu") ||
               lower.contains("asus") ||
               lower.contains("lenovo") ||
               lower.contains("knox") ||
               lower.contains("secspace") ||
               lower.contains("workprofile") ||
               lower == "android"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isRunning) return

        val rootNode = try {
            rootInActiveWindow ?: event.source
        } catch (e: Exception) {
            event.source
        } ?: return

        val eventPackage = try {
            event.packageName?.toString() ?: ""
        } catch (e: Exception) {
            ""
        }

        if (!isSettingsPackage(eventPackage)) return

        processActiveNode(rootNode)
    }

    private val isBoostMode: Boolean
        get() {
            return try {
                val prefs = getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
                prefs.getBoolean("turbo_mode", prefs.getBoolean("boost_mode", false))
            } catch (e: Exception) {
                false
            }
        }

    private val isCustomSpeedEnabled: Boolean
        get() {
            return try {
                val prefs = getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
                prefs.getBoolean("custom_speed_enabled", false)
            } catch (e: Exception) {
                false
            }
        }

    private fun getScanDelay(): Long {
        if (isCustomSpeedEnabled) {
            val prefs = try { getSharedPreferences("apex_prefs", Context.MODE_PRIVATE) } catch (e: Exception) { null }
            val key = if (currentMode == ServiceMode.FORCE_STOP) "stop_scan_delay" else "clean_scan_delay"
            return prefs?.getLong(key, 140L) ?: 140L
        }
        return if (isBoostMode) 70L else 140L
    }

    private fun getPageWait(): Long {
        if (isCustomSpeedEnabled) {
            val prefs = try { getSharedPreferences("apex_prefs", Context.MODE_PRIVATE) } catch (e: Exception) { null }
            val key = if (currentMode == ServiceMode.FORCE_STOP) "stop_page_wait" else "clean_page_wait"
            return prefs?.getLong(key, 450L) ?: 450L
        }
        return if (isBoostMode) 280L else 450L
    }

    private fun getStepPause(): Long {
        if (isCustomSpeedEnabled) {
            val prefs = try { getSharedPreferences("apex_prefs", Context.MODE_PRIVATE) } catch (e: Exception) { null }
            return prefs?.getLong("clean_step_pause", 350L) ?: 350L
        }
        return if (isBoostMode) 180L else 350L
    }

    private fun getNextAppDelay(baseDelayMs: Long): Long {
        if (isCustomSpeedEnabled) {
            val prefs = try { getSharedPreferences("apex_prefs", Context.MODE_PRIVATE) } catch (e: Exception) { null }
            val key = if (currentMode == ServiceMode.FORCE_STOP) "stop_next_delay" else "clean_next_delay"
            return prefs?.getLong(key, baseDelayMs) ?: baseDelayMs
        }
        return if (isBoostMode) baseDelayMs else (baseDelayMs * 2).coerceAtLeast(260L)
    }

    private fun getConfirmWait(retriesLeft: Int): Long {
        if (isCustomSpeedEnabled) {
            val prefs = try { getSharedPreferences("apex_prefs", Context.MODE_PRIVATE) } catch (e: Exception) { null }
            val base = prefs?.getLong("stop_confirm_wait", 150L) ?: 150L
            return when (retriesLeft) {
                3 -> base
                2 -> base * 2
                else -> base * 3
            }
        }
        return if (isBoostMode) {
            when (retriesLeft) {
                3 -> 80L
                2 -> 160L
                else -> 240L
            }
        } else {
            when (retriesLeft) {
                3 -> 150L
                2 -> 300L
                else -> 500L
            }
        }
    }

    private fun startPeriodicScan() {
        stopPeriodicScan()
        periodicScanRunnable = object : Runnable {
            override fun run() {
                if (!isRunning) return
                try {
                    val root = rootInActiveWindow
                    if (root != null) {
                        val pkg = root.packageName?.toString() ?: ""
                        if (isSettingsPackage(pkg)) {
                            processActiveNode(root)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (isRunning) {
                    val scanDelay = getScanDelay()
                    mainHandler.postDelayed(this, scanDelay)
                }
            }
        }
        mainHandler.post(periodicScanRunnable!!)
    }

    private fun stopPeriodicScan() {
        periodicScanRunnable?.let { mainHandler.removeCallbacks(it) }
        periodicScanRunnable = null
    }

    @Synchronized
    private fun processActiveNode(rootNode: AccessibilityNodeInfo) {
        if (!isRunning || isStepInProgress) return

        // Protection against screen transition race condition:
        // Ignore scans for the first 350ms after launching a new app settings intent to prevent reading stale previous app screens
        if (System.currentTimeMillis() - lastAppOpenTime < 350L) return

        if (currentMode == ServiceMode.FORCE_STOP) {
            handleForceStopEvent(rootNode)
        } else {
            handleClearCacheEvent(rootNode)
        }
    }

    private fun handleClearCacheEvent(rootNode: AccessibilityNodeInfo) {
        if (hasClickedConfirmClearCache) return

        val currentPkg = if (currentAppIndex in packageList.indices) packageList[currentAppIndex] else ""
        val minPageWait = getPageWait()
        val stepPause = getStepPause()
        val disabledGraceWindow = 2500L

        // Case 1: Clear Cache was already clicked -> check for confirmation dialogs
        if (hasClickedClearCache) {
            val confirmNode = findDialogConfirmNode(rootNode)
            if (confirmNode != null) {
                isStepInProgress = true
                val clicked = performClick(confirmNode)
                if (clicked) {
                    hasClickedConfirmClearCache = true
                    itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                    scheduleNextApp(120)
                    return
                } else {
                    isStepInProgress = false
                }
            }
            return
        }

        // Case 2: Xiaomi / MIUI / HyperOS popup menu (Clear Data bottom action was clicked)
        if (hasClickedClearData) {
            val miuiClearCacheNode = findClearCacheNode(rootNode)
            if (miuiClearCacheNode != null) {
                isStepInProgress = true
                val clicked = performClick(miuiClearCacheNode)
                if (clicked) {
                    hasClickedClearCache = true
                    startWatchdog()
                    pollForClearCacheConfirmationDialog(currentPkg, 3)
                    return
                } else {
                    isStepInProgress = false
                }
            }
            val confirmNode = findDialogConfirmNode(rootNode)
            if (confirmNode != null) {
                isStepInProgress = true
                val clicked = performClick(confirmNode)
                if (clicked) {
                    hasClickedClearCache = true
                    hasClickedConfirmClearCache = true
                    itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                    scheduleNextApp(120)
                    return
                } else {
                    isStepInProgress = false
                }
            }
            return
        }

        // Case 3: Inside Storage sub-screen (hasClickedStorage == true)
        if (hasClickedStorage) {
            val storageClearCacheNode = findClearCacheNode(rootNode)
            if (storageClearCacheNode != null) {
                if (!storageClearCacheNode.isEnabled) {
                    // Give grace period for OS cache calculation ("Computing...")
                    if (System.currentTimeMillis() - pageOpenedTime < minPageWait + disabledGraceWindow) return

                    val activeRoot = rootInActiveWindow
                    if (activeRoot != null) {
                        val activeClearCache = findClearCacheNode(activeRoot)
                        if (activeClearCache != null && activeClearCache.isEnabled) {
                            isStepInProgress = true
                            val clicked = performClick(activeClearCache)
                            if (clicked) {
                                hasClickedClearCache = true
                                startWatchdog()
                                pollForClearCacheConfirmationDialog(currentPkg, 3)
                                return
                            } else {
                                isStepInProgress = false
                            }
                        }
                    }

                    // Button remains disabled after full grace period -> Cache is already 0 B / clean!
                    hasClickedClearCache = true
                    hasClickedConfirmClearCache = true
                    itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                    scheduleNextApp(100)
                    return
                }

                isStepInProgress = true
                val clicked = performClick(storageClearCacheNode)
                if (clicked) {
                    hasClickedClearCache = true
                    startWatchdog()
                    pollForClearCacheConfirmationDialog(currentPkg, 3)
                    return
                } else {
                    isStepInProgress = false
                }
            } else if (scrollCountForClearCache < 4) {
                // Clear cache button not visible inside Storage -> Scroll Down!
                scrollCountForClearCache++
                isStepInProgress = true
                performScrollDown(rootNode)
                mainHandler.postDelayed({ isStepInProgress = false }, stepPause + 150L)
                return
            }

            // Check if confirmation dialog appeared inside Storage
            val confirmNode = findDialogConfirmNode(rootNode)
            if (confirmNode != null) {
                isStepInProgress = true
                val clicked = performClick(confirmNode)
                if (clicked) {
                    hasClickedClearCache = true
                    hasClickedConfirmClearCache = true
                    itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                    scheduleNextApp(120)
                    return
                } else {
                    isStepInProgress = false
                }
            }
            return
        }

        // Case 4: Main App Info Screen (hasClickedStorage == false and hasClickedClearData == false)
        // First check for standalone clear cache button matching strict resource ID suffixes
        val standaloneClearCacheNode = findStandaloneClearCacheNodeOnMainPage(rootNode)
        if (standaloneClearCacheNode != null) {
            if (!standaloneClearCacheNode.isEnabled) {
                if (System.currentTimeMillis() - pageOpenedTime < minPageWait + disabledGraceWindow) return
                hasClickedClearCache = true
                hasClickedConfirmClearCache = true
                itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                scheduleNextApp(100)
                return
            }

            isStepInProgress = true
            val clicked = performClick(standaloneClearCacheNode)
            if (clicked) {
                hasClickedClearCache = true
                startWatchdog()
                pollForClearCacheConfirmationDialog(currentPkg, 3)
                return
            } else {
                isStepInProgress = false
            }
        }

        // Check for Xiaomi / MIUI / HyperOS "Clear Data" bottom button
        val clearDataNode = findClearDataNode(rootNode)
        if (clearDataNode != null && clearDataNode.isEnabled) {
            isStepInProgress = true
            val clicked = performClick(clearDataNode)
            if (clicked) {
                hasClickedClearData = true
                pageOpenedTime = System.currentTimeMillis()
                startWatchdog()
                mainHandler.postDelayed({ isStepInProgress = false }, stepPause)
                return
            } else {
                isStepInProgress = false
            }
        }

        // Find "Storage" navigation item on main App Info screen
        val storageNode = findStorageNode(rootNode)
        if (storageNode != null) {
            isStepInProgress = true
            val clicked = performClick(storageNode)
            if (clicked) {
                hasClickedStorage = true
                pageOpenedTime = System.currentTimeMillis()
                startWatchdog()
                mainHandler.postDelayed({ isStepInProgress = false }, stepPause)
                return
            } else {
                isStepInProgress = false
            }
        } else if (scrollCountForStorage < 4) {
            // Storage item not visible -> Scroll Down! (EXCLUSIVELY FOR CLEAR_CACHE)
            scrollCountForStorage++
            isStepInProgress = true
            performScrollDown(rootNode)
            mainHandler.postDelayed({ isStepInProgress = false }, stepPause + 150L)
            return
        }
    }

    private fun findStandaloneClearCacheNodeOnMainPage(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // If "Storage" or "Clear Data" navigation node exists on the page, this is the main App Details page.
        // We should NEVER treat any element on main page as standalone clear cache if Storage/ClearData exists!
        if (findStorageNode(rootNode) != null || findClearDataNode(rootNode) != null) {
            return null
        }
        val nodeById = findNodeByIdSuffixes(rootNode, CLEAR_CACHE_ID_SUFFIXES)
        if (nodeById != null && isValidClearCacheNode(nodeById)) {
            return nodeById
        }
        return null
    }

    private fun handleForceStopEvent(rootNode: AccessibilityNodeInfo) {
        if (hasClickedConfirmForceStop) return

        val currentPkg = if (currentAppIndex in packageList.indices) packageList[currentAppIndex] else ""
        val minPageWait = getPageWait()
        val disabledGraceWindow = 1200L

        // Step 1: Check for confirmation dialog first if Force Stop was clicked
        if (hasClickedForceStop) {
            val confirmNode = findDialogConfirmNode(rootNode)
            if (confirmNode != null) {
                isStepInProgress = true
                val clicked = performClick(confirmNode)
                if (clicked) {
                    hasClickedConfirmForceStop = true
                    itemResultCallback?.invoke(currentPkg, true, ServiceMode.FORCE_STOP)
                    scheduleNextApp(120)
                    return
                } else {
                    isStepInProgress = false
                }
            }
        }

        // Step 2: Look for "Force Stop" button on app details page
        if (!hasClickedForceStop) {
            val forceStopNode = findForceStopNode(rootNode)
            if (forceStopNode != null) {
                if (!forceStopNode.isEnabled) {
                    if (System.currentTimeMillis() - pageOpenedTime < minPageWait + disabledGraceWindow) return

                    // Live double-check before concluding button is truly disabled
                    val activeRoot = rootInActiveWindow
                    if (activeRoot != null) {
                        val activeForceStop = findForceStopNode(activeRoot)
                        if (activeForceStop != null && activeForceStop.isEnabled) {
                            isStepInProgress = true
                            val clicked = performClick(activeForceStop)
                            if (clicked) {
                                hasClickedForceStop = true
                                startWatchdog()
                                pollForConfirmationDialog(currentPkg, 3)
                                return
                            } else {
                                isStepInProgress = false
                            }
                        }
                    }

                    hasClickedForceStop = true
                    hasClickedConfirmForceStop = true
                    itemResultCallback?.invoke(currentPkg, false, ServiceMode.FORCE_STOP)
                    scheduleNextApp(100)
                    return
                }

                isStepInProgress = true
                val clicked = performClick(forceStopNode)
                if (clicked) {
                    hasClickedForceStop = true
                    startWatchdog()

                    // Poll for confirmation dialog
                    pollForConfirmationDialog(currentPkg, 3)
                    return
                } else {
                    isStepInProgress = false
                }
            }
        }
    }

    private fun performScrollDown(rootNode: AccessibilityNodeInfo): Boolean {
        // SCROLLING IS STRLICTLY RESERVED ONLY FOR CLEAR_CACHE MODE
        if (currentMode != ServiceMode.CLEAR_CACHE) return false

        val scrollableNode = findScrollableNode(rootNode)
        if (scrollableNode != null) {
            try {
                val scrolled = scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                if (scrolled) return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val displayMetrics = resources.displayMetrics
                val startX = displayMetrics.widthPixels / 2f
                val startY = displayMetrics.heightPixels * 0.75f
                val endY = displayMetrics.heightPixels * 0.25f

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(startX, endY)
                }

                val stroke = GestureDescription.StrokeDescription(path, 0, 180)
                val gesture = GestureDescription.Builder().addStroke(stroke).build()
                return dispatchGesture(gesture, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isScrollable) return node

        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = findScrollableNode(child)
                if (result != null) return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun pollForConfirmationDialog(currentPkg: String, retriesLeft: Int) {
        if (!isRunning || hasClickedConfirmForceStop || retriesLeft <= 0) {
            if (isRunning && hasClickedForceStop && !hasClickedConfirmForceStop) {
                val activeRoot = rootInActiveWindow
                if (activeRoot != null) {
                    val forceStopNode = findForceStopNode(activeRoot)
                    if (forceStopNode != null && !forceStopNode.isEnabled) {
                        hasClickedConfirmForceStop = true
                        itemResultCallback?.invoke(currentPkg, true, ServiceMode.FORCE_STOP)
                        scheduleNextApp(120)
                        return
                    }
                }
                hasClickedConfirmForceStop = true
                itemResultCallback?.invoke(currentPkg, false, ServiceMode.FORCE_STOP)
                scheduleNextApp(100)
                return
            }
            isStepInProgress = false
            return
        }

        val delay = getConfirmWait(retriesLeft)

        mainHandler.postDelayed({
            if (isRunning && !hasClickedConfirmForceStop) {
                val activeRoot = rootInActiveWindow
                if (activeRoot != null) {
                    val confirmNode = findDialogConfirmNode(activeRoot)
                    if (confirmNode != null) {
                        val clicked = performClick(confirmNode)
                        if (clicked) {
                            hasClickedConfirmForceStop = true
                            itemResultCallback?.invoke(currentPkg, true, ServiceMode.FORCE_STOP)
                            scheduleNextApp(120)
                            return@postDelayed
                        }
                    }
                }
                pollForConfirmationDialog(currentPkg, retriesLeft - 1)
            } else {
                isStepInProgress = false
            }
        }, delay)
    }

    private fun pollForClearCacheConfirmationDialog(currentPkg: String, retriesLeft: Int) {
        if (!isRunning || hasClickedConfirmClearCache || retriesLeft <= 0) {
            if (isRunning && hasClickedClearCache && !hasClickedConfirmClearCache) {
                hasClickedConfirmClearCache = true
                itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                scheduleNextApp(120)
                return
            }
            isStepInProgress = false
            return
        }

        val delay = getConfirmWait(retriesLeft)

        mainHandler.postDelayed({
            if (isRunning && !hasClickedConfirmClearCache) {
                val activeRoot = rootInActiveWindow
                if (activeRoot != null) {
                    val confirmNode = findDialogConfirmNode(activeRoot)
                    if (confirmNode != null) {
                        val clicked = performClick(confirmNode)
                        if (clicked) {
                            hasClickedConfirmClearCache = true
                            itemResultCallback?.invoke(currentPkg, true, ServiceMode.CLEAR_CACHE)
                            scheduleNextApp(120)
                            return@postDelayed
                        }
                    }
                }
                pollForClearCacheConfirmationDialog(currentPkg, retriesLeft - 1)
            } else {
                isStepInProgress = false
            }
        }, delay)
    }

    private fun scheduleNextApp(delayMs: Long) {
        val actualDelay = getNextAppDelay(delayMs)
        mainHandler.postDelayed({
            isStepInProgress = false
            moveToNextApp()
        }, actualDelay)
    }

    private fun findStorageNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val nodeById = findNodeByIdSuffixes(rootNode, STORAGE_ID_SUFFIXES)
        if (nodeById != null && isValidStorageNode(nodeById)) {
            return nodeById
        }

        for (keyword in STORAGE_KEYWORDS) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                if (!nodes.isNullOrEmpty()) {
                    for (n in nodes) {
                        if (isValidStorageNode(n)) {
                            return n
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return searchStorageRecursively(rootNode)
    }

    private fun searchStorageRecursively(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = try { node.text?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        val contentDesc = try { node.contentDescription?.toString()?.trim() ?: "" } catch (e: Exception) { "" }

        val combined = "$text $contentDesc".lowercase()
        if (combined.isNotEmpty()) {
            for (keyword in STORAGE_KEYWORDS) {
                if (combined.contains(keyword.lowercase())) {
                    if (isValidStorageNode(node)) {
                        return node
                    }
                }
            }
        }

        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = searchStorageRecursively(child)
                if (result != null) return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun isValidStorageNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val text = try { node.text?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        val contentDesc = try { node.contentDescription?.toString()?.trim() ?: "" } catch (e: Exception) { "" }

        val combined = "$text $contentDesc".lowercase()
        val matchesStorage = STORAGE_KEYWORDS.any { combined.contains(it.lowercase()) }
        if (!matchesStorage) return false

        var p: AccessibilityNodeInfo? = node
        var depth = 0
        while (p != null && depth < 6) {
            if (canNodeBeClicked(p)) return true
            p = p.parent
            depth++
        }
        return false
    }

    private fun findClearCacheNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val nodeById = findNodeByIdSuffixes(rootNode, CLEAR_CACHE_ID_SUFFIXES)
        if (nodeById != null && isValidClearCacheNode(nodeById)) {
            return nodeById
        }

        for (keyword in CLEAR_CACHE_KEYWORDS) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                if (!nodes.isNullOrEmpty()) {
                    for (n in nodes) {
                        if (isValidClearCacheNode(n)) {
                            return n
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return searchClearCacheRecursively(rootNode)
    }

    private fun searchClearCacheRecursively(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = try { node.text?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        val contentDesc = try { node.contentDescription?.toString()?.trim() ?: "" } catch (e: Exception) { "" }

        val combined = "$text $contentDesc".lowercase()
        if (combined.isNotEmpty() && (text.length <= 60 || contentDesc.length <= 60)) {
            for (keyword in CLEAR_CACHE_KEYWORDS) {
                if (combined.contains(keyword.lowercase())) {
                    if (isValidClearCacheNode(node)) {
                        return node
                    }
                }
            }
        }

        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = searchClearCacheRecursively(child)
                if (result != null) return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun isValidClearCacheNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val text = try { node.text?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        val contentDesc = try { node.contentDescription?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        if (text.length > 60 || contentDesc.length > 60) return false

        val combined = "$text $contentDesc".lowercase()

        // 1. MUST NOT match Force Stop keywords!
        for (fsKw in FORCE_STOP_KEYWORDS) {
            val lowerFsKw = fsKw.lowercase()
            if (combined.contains(lowerFsKw)) {
                return false
            }
        }

        // 2. MUST NOT match Open / Launch / Uninstall / Disable action keywords!
        val excludeActionKeywords = listOf(
            "open", "launch", "فتح", "تشغيل", "ouvrir", "abrir", "öffnen", "apri", "открыть",
            "uninstall", "désinstaller", "desinstalar", "deinstallieren", "إلغاء التثبيت", "حذف التطبيق",
            "disable", "désactiver", "desactivar", "deaktivieren", "تعطيل", "إيقاف الخدمة"
        )
        for (actKw in excludeActionKeywords) {
            if (combined.contains(actKw)) {
                return false
            }
        }

        // 3. Exclude Storage Preference menu items & headers from falsely matching Clear Cache button
        val excludeMenuKeywords = listOf(
            "storage & cache", "storage and cache", "storage & storage info", "storage usage",
            "التخزين وذاكرة التخزين المؤقت", "التخزين والذاكرة المؤقتة", "التخزين و الذاكرة",
            "مساحة التخزين والتخزين المؤقت", "استخدام التخزين", "وحدة التخزين",
            "espace de stockage et cache", "almacenamiento y caché", "speicher & cache"
        )
        for (menuKw in excludeMenuKeywords) {
            if (combined.contains(menuKw)) {
                return false
            }
        }

        // 4. Exclude Clear Data / Clear Storage / Manage Space buttons from matching Clear Cache
        val excludeDataKeywords = listOf(
            "clear data", "clear all data", "clear storage", "manage space", "manage storage",
            "مسح البيانات", "مسح جميع البيانات", "مسح سعة التخزين", "إدارة المساحة", "تفريغ مساحة التخزين",
            "effacer les données", "limpiar datos", "borrar datos"
        )
        for (exKw in excludeDataKeywords) {
            if (combined.contains(exKw)) {
                return false
            }
        }

        var p: AccessibilityNodeInfo? = node
        var depth = 0
        while (p != null && depth < 6) {
            if (canNodeBeClicked(p)) return true
            p = p.parent
            depth++
        }
        return false
    }

    private fun findClearDataNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val nodeById = findNodeByIdSuffixes(rootNode, CLEAR_DATA_ID_SUFFIXES)
        if (nodeById != null && canNodeBeClicked(nodeById)) {
            return nodeById
        }

        for (keyword in CLEAR_DATA_KEYWORDS) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                if (!nodes.isNullOrEmpty()) {
                    for (n in nodes) {
                        if (canNodeBeClicked(n)) {
                            return n
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun findForceStopNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val nodeById = findNodeByIdSuffixes(rootNode, FORCE_STOP_ID_SUFFIXES)
        if (nodeById != null && isValidActionButton(nodeById)) {
            return nodeById
        }

        for (keyword in FORCE_STOP_KEYWORDS) {
            try {
                val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                if (!nodes.isNullOrEmpty()) {
                    for (n in nodes) {
                        if (isValidActionButton(n)) {
                            return n
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return searchForceStopRecursively(rootNode)
    }

    private fun searchForceStopRecursively(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (isNodeOpenButton(node)) return null

        val text = try { node.text?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        val contentDesc = try { node.contentDescription?.toString()?.trim() ?: "" } catch (e: Exception) { "" }

        if (text.length in 1..35 || contentDesc.length in 1..35) {
            for (keyword in FORCE_STOP_KEYWORDS) {
                val lowerKw = keyword.lowercase()
                if (text.lowercase().contains(lowerKw) || contentDesc.lowercase().contains(lowerKw)) {
                    if (isValidActionButton(node)) {
                        return node
                    }
                }
            }
        }

        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = searchForceStopRecursively(child)
                if (result != null) return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun findDialogConfirmNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val nodeById = findNodeByIdSuffixes(root, DIALOG_CONFIRM_ID_SUFFIXES)
        if (nodeById != null && isValidDialogConfirmNode(nodeById)) {
            return nodeById
        }

        return searchDialogButtonRecursively(root)
    }

    private fun isValidDialogConfirmNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        var p: AccessibilityNodeInfo? = node
        var depth = 0
        while (p != null && depth < 6) {
            if (canNodeBeClicked(p)) return true
            p = p.parent
            depth++
        }
        return false
    }

    private fun searchDialogButtonRecursively(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null

        val text = try { node.text?.toString()?.trim()?.lowercase() ?: "" } catch (e: Exception) { "" }
        
        if (text.isNotEmpty() && text.length <= 25) {
            for (keyword in DIALOG_CONFIRM_KEYWORDS) {
                val lowerKw = keyword.lowercase()
                if (text == lowerKw || text.contains(lowerKw)) {
                    if (isValidDialogConfirmNode(node)) {
                        return node
                    }
                }
            }
        }

        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = searchDialogButtonRecursively(child)
                if (result != null) return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun isNodeOpenButton(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val text = try { node.text?.toString()?.trim()?.lowercase() ?: "" } catch (e: Exception) { "" }
        val contentDesc = try { node.contentDescription?.toString()?.trim()?.lowercase() ?: "" } catch (e: Exception) { "" }

        val excludeOpenKeywords = listOf(
            "open", "launch", "فتح", "تشغيل", "ouvrir", "abrir", "öffnen", "apri", "открыть"
        )

        for (kw in excludeOpenKeywords) {
            if (text == kw || text.startsWith(kw) || contentDesc == kw || contentDesc.startsWith(kw)) {
                return true
            }
        }
        return false
    }

    private fun isValidActionButton(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (isNodeOpenButton(node)) return false

        val text = try { node.text?.toString()?.trim() ?: "" } catch (e: Exception) { "" }
        if (text.length > 35) return false

        var p: AccessibilityNodeInfo? = node
        var depth = 0
        while (p != null && depth < 6) {
            if (canNodeBeClicked(p)) return true
            p = p.parent
            depth++
        }
        return false
    }

    private fun canNodeBeClicked(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isClickable) return true
        return try {
            val className = node.className?.toString() ?: ""
            val isClickableClass = className.contains("Button", ignoreCase = true) || 
                                   className.contains("Action", ignoreCase = true) ||
                                   className.contains("Preference", ignoreCase = true) ||
                                   className.contains("Item", ignoreCase = true)
            isClickableClass || node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK }
        } catch (e: Exception) {
            false
        }
    }

    override fun onInterrupt() {
        stopCleaning()
    }

    private fun startWatchdog() {
        stopWatchdog()
        watchdogRunnable = Runnable {
            if (isRunning) {
                val currentPkg = if (currentAppIndex in packageList.indices) packageList[currentAppIndex] else null
                if (currentPkg != null) {
                    itemResultCallback?.invoke(currentPkg, false, currentMode)
                }
                isStepInProgress = false
                
                // Add 300ms buffer when watchdog fires to let OS clear activity queue and prevent domino skips
                mainHandler.postDelayed({
                    if (isRunning) {
                        moveToNextApp()
                    }
                }, 300L)
            }
        }
        val watchdogTimeout = if (isBoostMode) 3500L else 5000L
        watchdogRunnable?.let { mainHandler.postDelayed(it, watchdogTimeout) }
    }

    private fun stopWatchdog() {
        watchdogRunnable?.let { mainHandler.removeCallbacks(it) }
        watchdogRunnable = null
    }

    private fun moveToNextApp() {
        stopWatchdog()

        hasClickedStorage = false
        hasClickedClearData = false
        hasClickedClearCache = false
        hasClickedConfirmClearCache = false
        hasClickedForceStop = false
        hasClickedConfirmForceStop = false
        scrollCountForStorage = 0
        scrollCountForClearCache = 0
        isStepInProgress = false
        pageOpenedTime = System.currentTimeMillis()

        // Periodically run GC every 10 apps to keep JVM heap clean during massive batch operations
        if (currentAppIndex > 0 && currentAppIndex % 10 == 0) {
            try {
                System.gc()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currentAppIndex++
        processCurrentOrNextApp()
    }

    private fun processCurrentOrNextApp() {
        while (currentAppIndex < packageList.size) {
            val pkg = packageList[currentAppIndex]
            
            // Fast-Path for 0-Byte Apps in CLEAR_CACHE Mode:
            // If StorageStatsManager reports cache is ALREADY 0 B, skip opening Settings entirely!
            if (currentMode == ServiceMode.CLEAR_CACHE) {
                val liveCache = AppStorageHelper.getSingleAppCacheBytes(this, pkg)
                if (liveCache == 0L) {
                    progressCallback?.invoke(currentAppIndex, packageList.size, pkg)
                    itemResultCallback?.invoke(pkg, true, ServiceMode.CLEAR_CACHE)
                    currentAppIndex++
                    continue
                }
            }

            // Fast-Path for Stopped Apps in FORCE_STOP Mode:
            // If Android OS reports app is ALREADY stopped (FLAG_STOPPED), skip opening Settings entirely!
            if (currentMode == ServiceMode.FORCE_STOP) {
                if (AppStorageHelper.isAppStopped(this, pkg)) {
                    progressCallback?.invoke(currentAppIndex, packageList.size, pkg)
                    itemResultCallback?.invoke(pkg, true, ServiceMode.FORCE_STOP)
                    currentAppIndex++
                    continue
                }
            }

            val pm = packageManager
            val appLabel = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                appInfo.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkg
            }

            progressCallback?.invoke(currentAppIndex, packageList.size, pkg)
            openAppDetailsSettings(this, pkg)
            return
        }

        // All packages processed -> finish & return to app
        stopCleaning(returnToApp = true)
    }

    private fun findNodeByIdSuffixes(root: AccessibilityNodeInfo, suffixes: List<String>): AccessibilityNodeInfo? {
        for (suffix in suffixes) {
            val nodes = searchNodesByIdSuffixRecursively(root, suffix)
            if (!nodes.isNullOrEmpty()) {
                return nodes[0]
            }
        }
        return null
    }

    private fun searchNodesByIdSuffixRecursively(node: AccessibilityNodeInfo?, suffix: String): List<AccessibilityNodeInfo>? {
        if (node == null) return null
        val resId = try { node.viewIdResourceName?.lowercase() ?: "" } catch (e: Exception) { "" }
        if (resId.isNotEmpty()) {
            val idName = resId.substringAfter(":id/")
            if (idName == suffix || idName.endsWith(suffix)) {
                return listOf(node)
            }
        }

        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val res = searchNodesByIdSuffixRecursively(child, suffix)
                if (!res.isNullOrEmpty()) return res
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun performClick(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        var temp: AccessibilityNodeInfo? = node
        var depth = 0
        while (temp != null && depth < 6) {
            try {
                if (temp.isClickable || canNodeBeClicked(temp)) {
                    val clicked = temp.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clicked) return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            temp = temp.parent
            depth++
        }

        val actionClicked = try {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } catch (e: Exception) {
            false
        }

        if (actionClicked) return true

        val isGestureFallbackEnabled = try {
            val prefs = getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
            prefs.getBoolean("gesture_fallback_enabled", false)
        } catch (e: Exception) {
            false
        }

        // Fallback: Hardware gesture click (API 24+) if performAction(ACTION_CLICK) fails on custom OEM views
        if (isGestureFallbackEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val bounds = android.graphics.Rect()
                node.getBoundsInScreen(bounds)
                if (bounds.width() > 0 && bounds.height() > 0) {
                    val centerX = bounds.centerX().toFloat()
                    val centerY = bounds.centerY().toFloat()

                    val path = Path().apply {
                        moveTo(centerX, centerY)
                    }
                    val stroke = GestureDescription.StrokeDescription(path, 0, 50)
                    val gesture = GestureDescription.Builder().addStroke(stroke).build()
                    return dispatchGesture(gesture, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return false
    }

    companion object {
        var isRunning = false
            private set

        var currentMode = ServiceMode.CLEAR_CACHE
            private set
            
        var instance: CacheCleanerAccessibilityService? = null
            private set

        private var packageList = listOf<String>()
        private var currentAppIndex = 0
        
        var progressCallback: ((index: Int, total: Int, packageName: String) -> Unit)? = null
        var itemResultCallback: ((packageName: String, success: Boolean, mode: ServiceMode) -> Unit)? = null
        var completionCallback: (() -> Unit)? = null

        private val STORAGE_KEYWORDS = listOf(
            "Storage", 
            "Storage & cache", 
            "Storage & storage info",
            "Storage usage",
            "Internal storage",
            "التخزين", 
            "مكان التخزين", 
            "الذاكرة", 
            "مساحة التخزين",
            "سعة التخزين",
            "التخزين وذاكرة التخزين المؤقت",
            "التخزين والذاكرة المؤقتة",
            "مساحة التخزين والتخزين المؤقت",
            "ذاكرة التخزين",
            "استخدام التخزين",
            "وحدة التخزين",
            "التخزين الداخلي",
            "سعة التخزين والذاكرة",
            "Espace de stockage",
            "Stockage",
            "Stockage et cache",
            "Almacenamiento",
            "Almacenamiento y caché",
            "Memoria",
            "Speicher",
            "Speicherplatz",
            "Speicher & Cache",
            "Spazio di archiviazione",
            "Archiviazione",
            "Armazenamento",
            "Armazenamento e cache",
            "Память",
            "Хранилище",
            "Память и кэш",
            "Depolama",
            "Önbellek ve depolama",
            "Penyimpanan",
            "Penyimpanan & cache"
        )

        private val STORAGE_ID_SUFFIXES = listOf(
            "storage_settings",
            "storage_layout",
            "storage_button",
            "storage",
            "storage_pref",
            "storage_detail",
            "storage_item"
        )

        private val CLEAR_DATA_KEYWORDS = listOf(
            "Clear data",
            "مسح البيانات",
            "Effacer les données",
            "Limpiar datos",
            "Borrar datos",
            "Daten löschen",
            "Cancella dati",
            "Limpar dados",
            "Очистить данные"
        )

        private val CLEAR_DATA_ID_SUFFIXES = listOf(
            "clear_data_button",
            "button_clear_data",
            "clear_data"
        )

        private val CLEAR_CACHE_KEYWORDS = listOf(
            "Clear Cache", 
            "Clear cache", 
            "Clean cache",
            "CLEAR CACHE",
            "Wipe cache",
            "Delete cache",
            "مسح ذاكرة التخزين المؤقت", 
            "مسح ذاكرة التخزين المؤقتة",
            "مسح ذاكرة التخزين الموقته",
            "تفريغ ذاكرة التخزين المؤقت",
            "تفريغ الذاكرة المؤقتة",
            "مسح التخزين المؤقت",
            "مسح التخزين الموقته",
            "مسح الذاكرة المؤقتة",
            "مسح الذاكرة الموقته",
            "تنظيف الذاكرة المؤقتة",
            "تنظيف ذاكرة التخزين المؤقت",
            "تنظيف ذاكرة التخزين المؤقتة",
            "تنظيف الكاش",
            "مسح الكاش",
            "تفريغ الكاش",
            "حذف ذاكرة التخزين المؤقت",
            "حذف الكاش",
            "إزالة ذاكرة التخزين المؤقت",
            "مسح ذاكرة التخزين",
            "تفريغ ذاكرة التخزين",
            "مسح البيانات المؤقتة",
            "تفريغ البيانات المؤقتة",
            "مسح الملفات المؤقتة",
            "إزالة الملفات المؤقتة",
            "حذف الذاكرة المؤقتة",
            "إزالة الذاكرة المؤقتة",
            "Vider le cache",
            "Effacer le cache",
            "Supprimer le cache",
            "VIDER LE CACHE",
            "EFFACER LE CACHE",
            "Borrar caché",
            "Limpiar caché",
            "Eliminar caché",
            "Borrar memoria caché",
            "Limpiar memoria caché",
            "BORRAR CACHÉ",
            "Cache leeren",
            "Temporäre Dateien löschen",
            "Cache löschen",
            "CACHE LEEREN",
            "Svuota cache",
            "Cancella cache",
            "Elimina cache",
            "SVUOTA CACHE",
            "Limpar cache",
            "Limpar o cache",
            "Apagar cache",
            "LIMPAR CACHE",
            "Очистить кэш",
            "Стереть кэш",
            "Удалить кэш",
            "ОЧИСТИТЬ КЭШ",
            "Önbelleği temizle",
            "Önbelleği sil",
            "ÖNBELLEĞİ TEMİZLE",
            "Hapus cache",
            "Bersihkan cache",
            "Hapus memori cache",
            "HAPUS CACHE",
            "Xóa bộ nhớ đệm",
            "Xóa cache",
            "پاک کردن حافظه پنهان",
            "پاک کردن کش",
            "清除缓存",
            "清除快取",
            "快取清除",
            "緩存清除",
            "キャッシュを消去",
            "キャッシュを削除",
            "キャッシュ消去",
            "캐시 삭제",
            "캐시 지우기"
        )

        private val CLEAR_CACHE_ID_SUFFIXES = listOf(
            "clear_cache_button",
            "clear_cache",
            "btn_clear_cache",
            "button_clear_cache",
            "clear_cache_btn",
            "clear_cache_action",
            "clear_cache_container",
            "delete_cache_button",
            "delete_cache",
            "clean_cache_button",
            "clean_cache",
            "clear_cache_tv",
            "clear_cache_text"
        )

        private val FORCE_STOP_KEYWORDS = listOf(
            "Force stop",
            "Force Stop",
            "FORCE STOP",
            "إيقاف إجباري",
            "إيقاف اجباري",
            "فرض الإيقاف",
            "إيقاف",
            "توقيف إجباري",
            "توقيف اجباري",
            "توقيف",
            "إيقاف فرضياً",
            "توقف إجباري",
            "توقف اجباري",
            "إيقاف إجباري للتطبيق",
            "إيقاف اجباري للتطبيق",
            "Arrêt forcé",
            "Fuerzar detención",
            "Forzar detención",
            "Stoppen erzwingen",
            "Arresto forzato",
            "Forçar parada",
            "Остановить",
            "Закрыть"
        )

        private val FORCE_STOP_ID_SUFFIXES = listOf(
            "force_stop_button",
            "force_stop",
            "btn_force_stop",
            "button_force_stop",
            "force_stop_btn",
            "left_button",
            "right_button",
            "button1",
            "button2",
            "action_force_stop",
            "menu_force_stop",
            "stop_button",
            "btn_stop"
        )

        private val DIALOG_CONFIRM_KEYWORDS = listOf(
            "ok",
            "موافق",
            "حسناً",
            "حسنا",
            "إيقاف إجباري",
            "إيقاف اجباري",
            "فرض الإيقاف",
            "إيقاف",
            "توقيف",
            "تأكيد",
            "نعم",
            "موافقة",
            "yes",
            "confirm",
            "force stop",
            "FORCE STOP",
            "arrêter",
            "aceptar",
            "vider le cache",
            "clear cache",
            "CLEAR CACHE",
            "مسح التخزين المؤقت",
            "مسح ذاكرة التخزين المؤقت",
            "مسح الكاش",
            "تفريغ الكاش"
        )

        private val DIALOG_CONFIRM_ID_SUFFIXES = listOf(
            "button1",
            "button_ok",
            "ok_button",
            "confirm",
            "positive_button",
            "ok",
            "android:id/button1"
        )

        fun startCleaning(context: Context, packages: List<String>, mode: ServiceMode = ServiceMode.CLEAR_CACHE) {
            if (packages.isEmpty()) return
            packageList = packages
            currentAppIndex = 0
            currentMode = mode
            isRunning = true
            
            val inst = instance
            if (inst != null) {
                inst.processCurrentOrNextApp()
            } else {
                progressCallback?.invoke(0, packages.size, packages[0])
                openAppDetailsSettings(context, packages[0])
            }
            
            // Wait 1.5s (1500ms) on the first app's settings page before Accessibility starts clicking
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isRunning) return@postDelayed
                instance?.pageOpenedTime = System.currentTimeMillis()
                instance?.startPeriodicScan()
            }, 1500L)
        }

        fun stopCleaning(returnToApp: Boolean = false) {
            isRunning = false
            packageList = emptyList()
            currentAppIndex = 0
            val cb = completionCallback
            progressCallback = null
            itemResultCallback = null
            completionCallback = null
            
            instance?.apply {
                mainHandler.removeCallbacksAndMessages(null)
                stopPeriodicScan()
                stopWatchdog()
                hasClickedStorage = false
                hasClickedClearData = false
                hasClickedClearCache = false
                hasClickedConfirmClearCache = false
                hasClickedForceStop = false
                hasClickedConfirmForceStop = false
                scrollCountForStorage = 0
                scrollCountForClearCache = 0
                isStepInProgress = false
                pageOpenedTime = 0L
            }

            if (returnToApp) {
                instance?.bringAppToForeground()
            }

            AppStorageHelper.clearAllMemoryCaches()

            cb?.invoke()
        }

        private fun openAppDetailsSettings(context: Context, packageName: String) {
            instance?.lastAppOpenTime = System.currentTimeMillis()
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            instance?.startWatchdog()
        }
    }
}
