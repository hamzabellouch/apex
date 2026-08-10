package com.tkno.blueiris.ui.main

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.res.stringResource
import com.tkno.blueiris.R
import com.tkno.blueiris.model.AppCacheInfo
import com.tkno.blueiris.service.CacheCleanerAccessibilityService
import com.tkno.blueiris.service.ServiceMode
import com.tkno.blueiris.util.AppStorageHelper
import com.tkno.blueiris.util.RamInfo
import com.tkno.blueiris.ui.page.AppUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainTab {
    Stop, Clean, Apps, Menu
}

enum class SubScreen {
    None, Analyze, StopWhitelist, CleanWhitelist, CleanHistory, StopHistory
}

@Composable
fun MainScreen(
    isUsageAccessGranted: Boolean,
    isAccessibilityEnabled: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRequestAccessibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember(context) { context.getSharedPreferences("blueiris_prefs", android.content.Context.MODE_PRIVATE) }

    val initialTab = remember(prefs) {
        val isSwapped = prefs.getBoolean("tab_order_swapped", false)
        if (isSwapped) MainTab.Clean else MainTab.Stop
    }

    var currentTab by remember { mutableStateOf(initialTab) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.None) }

    // System Back Button handler for SubScreens (Whitelist, Analyze, etc.)
    BackHandler(enabled = currentSubScreen != SubScreen.None) {
        currentSubScreen = SubScreen.None
    }

    // Dynamic states for storage & RAM sizes
    var installedApps by remember { mutableStateOf(emptyList<AppCacheInfo>()) }
    var totalCacheBytes by remember { mutableStateOf(0L) }
    var totalStorageBytes by remember { mutableStateOf(0L) }
    var usedStorageBytes by remember { mutableStateOf(0L) }
    var ramInfo by remember { mutableStateOf(AppStorageHelper.getRamInfo(context)) }
    var isScanning by remember { mutableStateOf(false) }

    var isAutoUpdateEnabled by remember {
        mutableStateOf(prefs.getBoolean("auto_update_enabled", false))
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "auto_update_enabled") {
                isAutoUpdateEnabled = p.getBoolean("auto_update_enabled", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    AppUpdater(isAutoUpdateEnabled = isAutoUpdateEnabled)

    var isPermissionSuccessCompleted by remember {
        mutableStateOf(prefs.getBoolean("permission_success_completed", false))
    }

    val loadNavOrder = remember(prefs) {
        {
            val isSwapped = prefs.getBoolean("tab_order_swapped", false)
            if (isSwapped) {
                listOf(MainTab.Clean, MainTab.Stop, MainTab.Apps, MainTab.Menu)
            } else {
                listOf(MainTab.Stop, MainTab.Clean, MainTab.Apps, MainTab.Menu)
            }
        }
    }

    val reorderableTabs = remember {
        mutableStateListOf(*loadNavOrder().toTypedArray())
    }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragOffset by remember { mutableStateOf(0f) }
    var itemWidthPx by remember { mutableStateOf(0f) }
    val viewConfiguration = LocalViewConfiguration.current
    val haptic = LocalHapticFeedback.current

    val isTabsSwapped = reorderableTabs.firstOrNull() == MainTab.Clean

    var stopWhitelist by remember {
        mutableStateOf(prefs.getStringSet("stop_whitelist", emptySet()) ?: emptySet())
    }

    var cleanWhitelist by remember {
        mutableStateOf(prefs.getStringSet("clean_whitelist", emptySet()) ?: emptySet())
    }

    fun updateStopWhitelist(newSet: Set<String>) {
        stopWhitelist = newSet
        prefs.edit().putStringSet("stop_whitelist", newSet).apply()
    }

    fun updateCleanWhitelist(newSet: Set<String>) {
        cleanWhitelist = newSet
        prefs.edit().putStringSet("clean_whitelist", newSet).apply()
    }

    // Target User apps AND allowed pre-installed system apps for Stop action
    val stoppableUserAppsOnly = remember(installedApps) {
        installedApps.filter { it.isStoppable }
    }

    // Target cleanable apps for Clean action
    val cleanableAppsOnly = remember(installedApps) {
        installedApps.filter { it.isCleanable }
    }

    var isIgnoreTinyCache by remember {
        mutableStateOf(prefs.getBoolean("ignore_tiny_cache", false))
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "ignore_tiny_cache") {
                isIgnoreTinyCache = p.getBoolean("ignore_tiny_cache", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // ONLY apps that actually have cache (> 0 Bytes or >= 3 MB if ignore_tiny_cache enabled) for Clean actions (excluding clean whitelist)
    val appsWithCacheOnly = remember(cleanableAppsOnly, cleanWhitelist, isIgnoreTinyCache) {
        val minCacheBytes = if (isIgnoreTinyCache) 3 * 1024 * 1024L else 1L
        cleanableAppsOnly.filter { it.cacheBytes >= minCacheBytes && it.packageName !in cleanWhitelist }
    }

    // Total cache bytes excluding clean whitelist (strictly synchronized with appsWithCacheOnly)
    val filteredTotalCacheBytes = remember(appsWithCacheOnly) {
        appsWithCacheOnly.sumOf { it.cacheBytes }
    }

    // Tracking stopped & cleaned packages to update counts and state in real-time across session
    var stoppedPackages by remember { mutableStateOf(setOf<String>()) }
    var cleanedPackages by remember { mutableStateOf(setOf<String>()) }
    var successfulPackages by remember { mutableStateOf(setOf<String>()) }

    fun applySessionOverrides(apps: List<AppCacheInfo>): List<AppCacheInfo> {
        if (stoppedPackages.isEmpty() && cleanedPackages.isEmpty()) return apps
        return apps.map { app ->
            var modified = app
            if (app.packageName in stoppedPackages) {
                modified = modified.copy(isStopped = true)
            }
            if (app.packageName in cleanedPackages) {
                modified = modified.copy(cacheBytes = 0L, cacheSizeString = "0 KB")
            }
            modified
        }
    }

    // ONLY apps that are active and NOT force-stopped and NOT in stop whitelist
    val activeRunningUserApps = remember(stoppableUserAppsOnly, stoppedPackages, stopWhitelist) {
        stoppableUserAppsOnly.filter { !it.isStopped && it.packageName !in stoppedPackages && it.packageName !in stopWhitelist }
    }

    // Accessibility cleaning progress states
    var showCleaningOverlay by remember { mutableStateOf(false) }
    var currentCleaningAppIndex by remember { mutableStateOf(0) }
    var currentCleaningAppName by remember { mutableStateOf("") }
    var currentCleaningPackage by remember { mutableStateOf<String?>(null) }
    var currentProcessingPackages by remember { mutableStateOf(emptyList<String>()) }
    var cleaningProgress by remember { mutableStateOf(0f) }

    // Dialog trigger
    var showAccessibilityPromptDialog by remember { mutableStateOf(false) }
    var accessibilityPromptMode by remember { mutableStateOf(ServiceMode.CLEAR_CACHE) }

    var showOperationDoneScreen by remember { mutableStateOf(false) }
    var completedActionType by remember { mutableStateOf("STOP") }
    var completedAppsCount by remember { mutableStateOf(0) }
    var completedCleanedBytes by remember { mutableStateOf(0L) }

    val isShowingPermissionScreen = !isUsageAccessGranted ||
            (showAccessibilityPromptDialog && !isAccessibilityEnabled) ||
            (isUsageAccessGranted && isAccessibilityEnabled && !isPermissionSuccessCompleted) ||
            showOperationDoneScreen

    val accentBlue = Color(0xFF48AFFF)
    val stopOrange = Color(0xFFFF6D00)
    val androidGreen = Color(0xFF3DDC84)
    val menuGray = MaterialTheme.colorScheme.onSurface
    val appBackground = MaterialTheme.colorScheme.background

    // Load actual apps & system metrics ONLY when app is actively open and in focus (resumes on foreground, halts completely when paused/closed)
    LaunchedEffect(lifecycleOwner, isUsageAccessGranted, showCleaningOverlay) {
        if (isUsageAccessGranted && !showCleaningOverlay) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                isScanning = true
                // ULTRA-FAST (0ms) Instant initial fetch when user enters/resumes app
                val initialApps = applySessionOverrides(AppStorageHelper.getInstalledAppsWithCache(context, forceRefresh = true))
                installedApps = initialApps
                totalCacheBytes = initialApps.sumOf { it.cacheBytes }
                totalStorageBytes = withContext(Dispatchers.IO) { AppStorageHelper.getTotalStorageBytes(context) }
                usedStorageBytes = withContext(Dispatchers.IO) { AppStorageHelper.getUsedStorageBytes(context) }
                ramInfo = AppStorageHelper.getRamInfo(context)
                isScanning = false

                // Real-time live update loop (Runs ONLY while app is actively open in foreground)
                while (true) {
                    ramInfo = AppStorageHelper.getRamInfo(context)

                    val rawApps = AppStorageHelper.getInstalledAppsWithCache(context, forceRefresh = true)
                    
                    // Release session overrides for apps that have actually restarted or generated cache in OS
                    if (stoppedPackages.isNotEmpty()) {
                        stoppedPackages = stoppedPackages.filterTo(mutableSetOf()) { pkg ->
                            val raw = rawApps.find { it.packageName == pkg }
                            raw != null && raw.isStopped
                        }
                    }
                    if (cleanedPackages.isNotEmpty()) {
                        cleanedPackages = cleanedPackages.filterTo(mutableSetOf()) { pkg ->
                            val raw = rawApps.find { it.packageName == pkg }
                            raw != null && raw.cacheBytes == 0L
                        }
                    }

                    val apps = applySessionOverrides(rawApps)
                    val totalBytes = apps.sumOf { it.cacheBytes }
                    val totalStorage = withContext(Dispatchers.IO) { AppStorageHelper.getTotalStorageBytes(context) }
                    val usedStorage = withContext(Dispatchers.IO) { AppStorageHelper.getUsedStorageBytes(context) }

                    val isDataChanged = installedApps.size != apps.size ||
                            totalCacheBytes != totalBytes ||
                            totalStorageBytes != totalStorage ||
                            usedStorageBytes != usedStorage ||
                            installedApps.zip(apps).any { (old, new) ->
                                old.packageName != new.packageName || old.cacheBytes != new.cacheBytes || old.isStopped != new.isStopped
                            }

                    if (isDataChanged) {
                        installedApps = apps
                        totalCacheBytes = totalBytes
                        totalStorageBytes = totalStorage
                        usedStorageBytes = usedStorage
                    }

                    kotlinx.coroutines.delay(1500L) // Continuous live pulse
                }
            }
        }
    }

    // Set up progress callbacks from the accessibility service
    LaunchedEffect(showCleaningOverlay) {
        if (showCleaningOverlay) {
            successfulPackages = emptySet()
            val initialCacheMap = installedApps.associate { it.packageName to it.cacheBytes }

            CacheCleanerAccessibilityService.progressCallback = { index, total, packageName ->
                currentCleaningAppIndex = index + 1
                currentCleaningPackage = packageName
                
                val pm = context.packageManager
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    appInfo.loadLabel(pm).toString()
                } catch (e: Exception) {
                    packageName
                }
                currentCleaningAppName = appLabel
                cleaningProgress = index.toFloat() / total.toFloat()
            }

            CacheCleanerAccessibilityService.itemResultCallback = { packageName, success, mode ->
                if (success) {
                    successfulPackages = successfulPackages + packageName
                    if (mode == ServiceMode.FORCE_STOP) {
                        stoppedPackages = stoppedPackages + packageName
                    } else {
                        cleanedPackages = cleanedPackages + packageName
                    }
                    installedApps = applySessionOverrides(installedApps)
                    totalCacheBytes = installedApps.sumOf { it.cacheBytes }
                }
            }

            CacheCleanerAccessibilityService.completionCallback = {
                scope.launch(Dispatchers.Main) {
                    val targetSet = successfulPackages

                    val finishedMode = CacheCleanerAccessibilityService.currentMode
                    completedActionType = if (finishedMode == ServiceMode.FORCE_STOP) "STOP" else "CLEAN"
                    completedAppsCount = targetSet.size
                    completedCleanedBytes = targetSet.sumOf { pkg -> initialCacheMap[pkg] ?: 0L }

                    // Record history entries into HistoryManager ONLY for actually successful packages
                    val pm = context.packageManager
                    val now = System.currentTimeMillis()
                    if (finishedMode == ServiceMode.FORCE_STOP) {
                        val entries = targetSet.map { pkg ->
                            val label = try {
                                val info = pm.getApplicationInfo(pkg, 0)
                                info.loadLabel(pm).toString()
                            } catch (e: Exception) {
                                pkg
                            }
                            com.tkno.blueiris.util.StopHistoryEntry(packageName = pkg, appName = label, timestamp = now)
                        }
                        com.tkno.blueiris.util.HistoryManager.addStopRecords(context, entries)
                    } else {
                        val entries = targetSet.map { pkg ->
                            val appInfo = installedApps.find { it.packageName == pkg }
                            val label = appInfo?.name ?: try {
                                val info = pm.getApplicationInfo(pkg, 0)
                                info.loadLabel(pm).toString()
                            } catch (e: Exception) {
                                pkg
                            }
                            val bytes = initialCacheMap[pkg] ?: appInfo?.cacheBytes ?: 0L
                            com.tkno.blueiris.util.CleanHistoryEntry(packageName = pkg, appName = label, bytesCleared = bytes, timestamp = now)
                        }
                        com.tkno.blueiris.util.HistoryManager.addCleanRecords(context, entries)
                    }

                    installedApps = applySessionOverrides(installedApps)
                    totalCacheBytes = installedApps.sumOf { it.cacheBytes }

                    showCleaningOverlay = false
                    currentCleaningPackage = null
                    currentCleaningAppIndex = 0
                    currentCleaningAppName = ""
                    currentSubScreen = SubScreen.None
                    showOperationDoneScreen = true

                    // Asynchronous background sync with OS StorageStats
                    scope.launch(Dispatchers.IO) {
                        val freshApps = applySessionOverrides(AppStorageHelper.getInstalledAppsWithCache(context, forceRefresh = true))
                        val freshTotalBytes = freshApps.sumOf { it.cacheBytes }
                        val freshTotalStorage = AppStorageHelper.getTotalStorageBytes(context)
                        val freshUsedStorage = AppStorageHelper.getUsedStorageBytes(context)
                        
                        withContext(Dispatchers.Main) {
                            installedApps = freshApps
                            totalCacheBytes = freshTotalBytes
                            totalStorageBytes = freshTotalStorage
                            usedStorageBytes = freshUsedStorage
                        }
                    }
                }
            }
        } else {
            CacheCleanerAccessibilityService.stopCleaning()
            currentCleaningPackage = null
        }
    }

    val unselectedNavColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        bottomBar = {
            if (currentSubScreen == SubScreen.None && !isShowingPermissionScreen) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                    if (isScanning) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = when (currentTab) {
                                MainTab.Stop -> stopOrange
                                MainTab.Clean -> accentBlue
                                MainTab.Apps -> androidGreen
                                MainTab.Menu -> accentBlue
                            },
                            trackColor = Color.Transparent
                        )
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .height(72.dp)
                            .onGloballyPositioned { coordinates ->
                                val totalWidth = coordinates.size.width.toFloat()
                                if (totalWidth > 0) {
                                    itemWidthPx = totalWidth / 4f
                                }
                            }
                    ) {
                        reorderableTabs.forEachIndexed { index, tab ->
                            val isSelected = currentTab == tab
                            val isDragging = draggingIndex == index
                            val canDrag = index < 2

                            val (tabLabel, tabIcon, tabColor) = when (tab) {
                                MainTab.Stop -> Triple(stringResource(R.string.nav_stop), Icons.Default.Block, stopOrange)
                                MainTab.Clean -> Triple(stringResource(R.string.nav_clean), Icons.Default.CleaningServices, accentBlue)
                                MainTab.Apps -> Triple(stringResource(R.string.nav_apps), Icons.Default.Android, androidGreen)
                                MainTab.Menu -> Triple(stringResource(R.string.nav_menu), Icons.Default.Menu, menuGray)
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon,
                                        contentDescription = tabLabel,
                                        tint = if (isSelected) tabColor else unselectedNavColor
                                    )
                                },
                                label = {
                                    Text(
                                        text = tabLabel,
                                        color = if (isSelected) tabColor else unselectedNavColor,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = tabColor.copy(alpha = 0.2f),
                                    selectedIconColor = tabColor,
                                    selectedTextColor = tabColor,
                                    unselectedIconColor = unselectedNavColor,
                                    unselectedTextColor = unselectedNavColor
                                ),
                                modifier = Modifier
                                    .graphicsLayer {
                                        if (isDragging) {
                                            translationX = currentDragOffset
                                            scaleX = 1.12f
                                            scaleY = 1.12f
                                        }
                                    }
                                    .then(
                                        if (canDrag) {
                                            Modifier.pointerInput(index) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(requireUnconsumed = false)
                                                    var isLongPressActive = false

                                                    val longPressTimer = scope.launch {
                                                        delay(viewConfiguration.longPressTimeoutMillis)
                                                        isLongPressActive = true
                                                        draggingIndex = index
                                                        currentDragOffset = 0f
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }

                                                    val pointer = down.id
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull { it.id == pointer }

                                                        if (change == null || !change.pressed) {
                                                            longPressTimer.cancel()
                                                            if (isLongPressActive) {
                                                                draggingIndex = null
                                                                currentDragOffset = 0f
                                                            }
                                                            break
                                                        }

                                                        if (!isLongPressActive) {
                                                            val diff = change.position - down.position
                                                            if (diff.getDistance() > viewConfiguration.touchSlop) {
                                                                longPressTimer.cancel()
                                                            }
                                                        } else {
                                                            change.consume()
                                                            val deltaX = change.position.x - change.previousPosition.x
                                                            val activeIndex = draggingIndex ?: break
                                                            currentDragOffset += deltaX

                                                            val threshold = if (itemWidthPx > 0f) itemWidthPx * 0.5f else 100f

                                                            if (currentDragOffset > threshold && activeIndex == 0) {
                                                                val temp = reorderableTabs[0]
                                                                reorderableTabs[0] = reorderableTabs[1]
                                                                reorderableTabs[1] = temp
                                                                draggingIndex = 1
                                                                currentDragOffset -= itemWidthPx
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                prefs.edit().putBoolean("tab_order_swapped", reorderableTabs[0] == MainTab.Clean).apply()
                                                            } else if (currentDragOffset < -threshold && activeIndex == 1) {
                                                                val temp = reorderableTabs[1]
                                                                reorderableTabs[1] = reorderableTabs[0]
                                                                reorderableTabs[0] = temp
                                                                draggingIndex = 0
                                                                currentDragOffset += itemWidthPx
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                prefs.edit().putBoolean("tab_order_swapped", reorderableTabs[0] == MainTab.Clean).apply()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }
    },
        containerColor = appBackground,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackground)
                .padding(if (currentSubScreen == SubScreen.None && !isShowingPermissionScreen) paddingValues else PaddingValues(0.dp))
        ) {
            if (!isUsageAccessGranted) {
                UsageAccessPermissionScreen(
                    onContinueClick = onRequestUsageAccess
                )
            } else {
                AnimatedContent(
                    targetState = currentSubScreen,
                    transitionSpec = {
                        if (targetState != SubScreen.None) {
                            (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 10 }) togetherWith
                                    fadeOut(animationSpec = tween(180))
                        } else {
                            fadeIn(animationSpec = tween(200)) togetherWith
                                    (fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180)) { it / 10 })
                        }
                    },
                    label = "SubScreenTransition"
                ) { subScreen ->
                    when (subScreen) {
                        SubScreen.Analyze -> {
                            AnalyzeScreen(
                                installedApps = appsWithCacheOnly,
                                totalCacheBytes = filteredTotalCacheBytes,
                                currentCleaningPackage = currentCleaningPackage,
                                onBackClick = { currentSubScreen = SubScreen.None },
                                onClearClick = {
                                    if (!isUsageAccessGranted) {
                                        onRequestUsageAccess()
                                    } else if (!isAccessibilityEnabled) {
                                        accessibilityPromptMode = ServiceMode.CLEAR_CACHE
                                        showAccessibilityPromptDialog = true
                                    } else {
                                         val packages = appsWithCacheOnly.map { it.packageName }
                                         if (packages.isNotEmpty()) {
                                             currentProcessingPackages = packages
                                             completedCleanedBytes = filteredTotalCacheBytes
                                             CacheCleanerAccessibilityService.startCleaning(context, packages, mode = ServiceMode.CLEAR_CACHE)
                                             showCleaningOverlay = true
                                         } else {
                                            Toast.makeText(context, context.getString(R.string.toast_all_cache_cleaned), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                        SubScreen.StopWhitelist -> {
                            WhitelistScreen(
                                type = WhitelistType.STOP,
                                whitelistedPackages = stopWhitelist,
                                allApps = stoppableUserAppsOnly,
                                onAddPackages = { pkgs -> updateStopWhitelist(stopWhitelist + pkgs) },
                                onRemovePackage = { pkg -> updateStopWhitelist(stopWhitelist - pkg) },
                                onBackClick = { currentSubScreen = SubScreen.None }
                            )
                        }
                        SubScreen.CleanWhitelist -> {
                            WhitelistScreen(
                                type = WhitelistType.CLEAN,
                                whitelistedPackages = cleanWhitelist,
                                allApps = cleanableAppsOnly,
                                onAddPackages = { pkgs -> updateCleanWhitelist(cleanWhitelist + pkgs) },
                                onRemovePackage = { pkg -> updateCleanWhitelist(cleanWhitelist - pkg) },
                                onBackClick = { currentSubScreen = SubScreen.None }
                            )
                        }
                        SubScreen.CleanHistory -> {
                            HistoryScreen(
                                mode = HistoryMode.CLEAN,
                                onBackClick = { currentSubScreen = SubScreen.None }
                            )
                        }
                        SubScreen.StopHistory -> {
                            HistoryScreen(
                                mode = HistoryMode.STOP,
                                onBackClick = { currentSubScreen = SubScreen.None }
                            )
                        }
                        SubScreen.None -> {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))) togetherWith
                                            fadeOut(animationSpec = tween(180))
                                },
                                label = "TabTransition"
                            ) { tab ->
                                when (tab) {
                                    MainTab.Stop -> {
                                        StopScreen(
                                            installedApps = activeRunningUserApps,
                                            ramInfo = ramInfo,
                                            onOpenWhitelist = { currentSubScreen = SubScreen.StopWhitelist },
                                            onOpenHistory = { currentSubScreen = SubScreen.StopHistory },
                                            onAnalyzeStopClick = {
                                                if (!isUsageAccessGranted) {
                                                    onRequestUsageAccess()
                                                } else if (!isAccessibilityEnabled) {
                                                    accessibilityPromptMode = ServiceMode.FORCE_STOP
                                                    showAccessibilityPromptDialog = true
                                                } else {
                                                    val packages = activeRunningUserApps.map { it.packageName }
                                                    if (packages.isNotEmpty()) {
                                                        currentProcessingPackages = packages
                                                        CacheCleanerAccessibilityService.startCleaning(context, packages, mode = ServiceMode.FORCE_STOP)
                                                        showCleaningOverlay = true
                                                    } else {
                                                        Toast.makeText(context, context.getString(R.string.toast_all_bg_apps_stopped), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    MainTab.Clean -> {
                                        CleanScreen(
                                            totalStorageBytes = totalStorageBytes,
                                            usedStorageBytes = usedStorageBytes,
                                            totalCacheBytes = filteredTotalCacheBytes,
                                            onOpenWhitelist = { currentSubScreen = SubScreen.CleanWhitelist },
                                            onOpenHistory = { currentSubScreen = SubScreen.CleanHistory },
                                            onAnalyzeClick = {
                                                if (!isUsageAccessGranted) {
                                                    onRequestUsageAccess()
                                                } else if (!isAccessibilityEnabled) {
                                                    accessibilityPromptMode = ServiceMode.CLEAR_CACHE
                                                    showAccessibilityPromptDialog = true
                                                } else {
                                                     val packages = appsWithCacheOnly.map { it.packageName }
                                                     if (packages.isNotEmpty()) {
                                                         currentProcessingPackages = packages
                                                         completedCleanedBytes = filteredTotalCacheBytes
                                                         CacheCleanerAccessibilityService.startCleaning(context, packages, mode = ServiceMode.CLEAR_CACHE)
                                                         showCleaningOverlay = true
                                                     } else {
                                                        Toast.makeText(context, context.getString(R.string.toast_all_cache_cleaned), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    MainTab.Apps -> {
                                        AppsScreen(
                                            installedApps = installedApps,
                                            currentCleaningPackage = currentCleaningPackage
                                        )
                                    }
                                    MainTab.Menu -> {
                                        MenuScreen()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fixed Bottom-Right Action Button for Stop and Clean tabs (remains stationary during tab transitions)
            if (currentSubScreen == SubScreen.None && !isShowingPermissionScreen && (currentTab == MainTab.Stop || currentTab == MainTab.Clean)) {
                val isStopTab = currentTab == MainTab.Stop
                val buttonIconColor = if (isStopTab) stopOrange else accentBlue
                val buttonBgColor = buttonIconColor.copy(alpha = 0.2f)
                val buttonIcon = if (isStopTab) Icons.Default.Block else Icons.Default.CleaningServices
                val buttonIconDesc = stringResource(id = if (isStopTab) R.string.stop_button_icon_desc else R.string.clean_button_icon_desc)
                val buttonText = stringResource(id = if (isStopTab) R.string.stop_start_button else R.string.clean_start_button)
                val onButtonClick = {
                    if (isStopTab) {
                        if (!isUsageAccessGranted) {
                            onRequestUsageAccess()
                        } else if (!isAccessibilityEnabled) {
                            accessibilityPromptMode = ServiceMode.FORCE_STOP
                            showAccessibilityPromptDialog = true
                        } else {
                            val packages = activeRunningUserApps.map { it.packageName }
                            if (packages.isNotEmpty()) {
                                currentProcessingPackages = packages
                                CacheCleanerAccessibilityService.startCleaning(context, packages, mode = ServiceMode.FORCE_STOP)
                                showCleaningOverlay = true
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_all_bg_apps_stopped), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        if (!isUsageAccessGranted) {
                            onRequestUsageAccess()
                        } else if (!isAccessibilityEnabled) {
                            accessibilityPromptMode = ServiceMode.CLEAR_CACHE
                            showAccessibilityPromptDialog = true
                        } else {
                            val packages = appsWithCacheOnly.map { it.packageName }
                            if (packages.isNotEmpty()) {
                                currentProcessingPackages = packages
                                completedCleanedBytes = filteredTotalCacheBytes
                                CacheCleanerAccessibilityService.startCleaning(context, packages, mode = ServiceMode.CLEAR_CACHE)
                                showCleaningOverlay = true
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_all_cache_cleaned), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = onButtonClick,
                    containerColor = buttonBgColor,
                    contentColor = buttonIconColor,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = buttonIconDesc,
                        tint = buttonIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Accessibility Service Permission Screen
            if (showAccessibilityPromptDialog && !isAccessibilityEnabled) {
                AccessibilityPermissionScreen(
                    descriptionResId = if (accessibilityPromptMode == ServiceMode.FORCE_STOP)
                        R.string.accessibility_permission_desc1_stop
                    else
                        R.string.accessibility_permission_desc1,
                    onContinueClick = {
                        showAccessibilityPromptDialog = false
                        onRequestAccessibility()
                    },
                    onCancelClick = {
                        showAccessibilityPromptDialog = false
                    }
                )
            }

            // Final Permission Success Screen (Appears when both permissions are granted for the first time)
            if (isUsageAccessGranted && isAccessibilityEnabled && !isPermissionSuccessCompleted) {
                PermissionSuccessScreen(
                    onDoneClick = {
                        prefs.edit().putBoolean("permission_success_completed", true).apply()
                        isPermissionSuccessCompleted = true
                    }
                )
            }

            // Operation Done Screen (Appears after Start Stop or Start Clean finishes)
            if (showOperationDoneScreen) {
                OperationDoneScreen(
                    actionType = completedActionType,
                    completedAppsCount = completedAppsCount,
                    cleanedCacheBytes = completedCleanedBytes,
                    onDoneClick = {
                        showOperationDoneScreen = false
                        stoppedPackages = emptySet()
                        cleanedPackages = emptySet()
                        AppStorageHelper.invalidateCache()
                    }
                )
            }
        }
    }
}
