package com.tkno.apex.util

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.storage.StorageManager
import android.util.LruCache
import com.tkno.apex.model.AppCacheInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class RamInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedString: String,
    val freeString: String,
    val totalString: String
)

object AppStorageHelper {

    @Volatile
    private var cachedAppsList: List<AppCacheInfo>? = null
    @Volatile
    private var cachedPackageCount: Int = -1

    private val iconCache = LruCache<String, Drawable>(200)

    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAllowedStoppableSystemApp(packageName: String): Boolean {
        val lower = packageName.lowercase()
        
        // Critical system components that must NEVER be force stopped
        if (lower.contains("systemui") || 
            lower.contains("settings") || 
            lower == "android" || 
            lower.contains("play.services") || 
            lower.contains("gms") || 
            lower.contains("incallui")) {
            return false
        }

        // Allowed pre-installed user apps explicitly requested
        val allowedKeywords = listOf(
            "youtube",
            "chrome",
            "gm",          // Gmail (com.google.android.gm)
            "gmail",
            "email",
            "myfiles",
            "fileexplorer",
            "filemanager",
            "finder",
            "files",
            "gallery",
            "photos",
            "facebook",
            "katana",
            "instagram",
            "whatsapp",
            "calendar",
            "accessibility",
            "talkback",
            "contacts",
            "messaging",
            "mms",
            "messages",
            "phone",
            "dialer",
            "telephony",
            "telecom",
            "clock",
            "deskclock",
            "googlequicksearchbox",
            "vending",      // Google Play Store (com.android.vending)
            "playstore"
        )

        return allowedKeywords.any { lower.contains(it) }
    }

    private fun isAllowedCleanableSystemApp(packageName: String): Boolean {
        val lower = packageName.lowercase()
        
        // Critical system components that must NEVER be cleared
        if (lower.contains("systemui") || 
            lower == "android" || 
            lower.contains("play.services") || 
            lower.contains("gms") || 
            lower.contains("incallui")) {
            return false
        }

        // Settings IS cleanable for cache
        if (lower.contains("settings")) {
            return true
        }

        // Allowed pre-installed user apps explicitly requested
        val allowedKeywords = listOf(
            "youtube",
            "chrome",
            "gm",          // Gmail (com.google.android.gm)
            "gmail",
            "email",
            "myfiles",
            "fileexplorer",
            "filemanager",
            "finder",
            "files",
            "gallery",
            "photos",
            "facebook",
            "katana",
            "instagram",
            "whatsapp",
            "calendar",
            "accessibility",
            "talkback",
            "contacts",
            "messaging",
            "mms",
            "messages",
            "phone",
            "dialer",
            "telephony",
            "telecom",
            "clock",
            "deskclock",
            "googlequicksearchbox",
            "vending",      // Google Play Store (com.android.vending)
            "playstore"
        )

        return allowedKeywords.any { lower.contains(it) }
    }

    /**
     * Ultra-Fast Parallel Smart Fetch:
     * Reads installed packages and evaluates ApplicationInfo flags concurrently across background threads.
     * Caches app icons using LruCache to prevent costly IPC loadIcon calls.
     */
    suspend fun getInstalledAppsWithCache(context: Context, forceRefresh: Boolean = false): List<AppCacheInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager

        val packages = try {
            packageManager.getInstalledPackages(0)
        } catch (e: Exception) {
            emptyList()
        }

        if (!forceRefresh && cachedAppsList != null && packages.size == cachedPackageCount) {
            return@withContext cachedAppsList!!
        }

        val storageStatsManager = try {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        } catch (e: Exception) {
            null
        }
        val storageUuid = StorageManager.UUID_DEFAULT
        val myUserHandle = Process.myUserHandle()
        val selfPackageName = context.packageName

        val appList = coroutineScope {
            packages.map { pkg ->
                async(Dispatchers.IO) {
                    val appInfo = try {
                        packageManager.getApplicationInfo(pkg.packageName, 0)
                    } catch (e: Exception) {
                        pkg.applicationInfo ?: return@async null
                    }

                    if (pkg.packageName == selfPackageName) {
                        return@async null
                    }

                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isStopped = (appInfo.flags and ApplicationInfo.FLAG_STOPPED) != 0
                    val isStoppable = !isSystemApp || isAllowedStoppableSystemApp(pkg.packageName)
                    val isCleanable = !isSystemApp || isAllowedCleanableSystemApp(pkg.packageName)
                    val installTime = pkg.firstInstallTime.takeIf { it > 0 } ?: pkg.lastUpdateTime

                    val (cacheBytes, appSizeBytes) = if (storageStatsManager != null) {
                        try {
                            val stats = storageStatsManager.queryStatsForPackage(storageUuid, pkg.packageName, myUserHandle)
                            val appSize = stats.appBytes + stats.dataBytes
                            Pair(stats.cacheBytes, appSize)
                        } catch (e: Exception) {
                            val apkSize = try { java.io.File(appInfo.sourceDir).length() } catch (ex: Exception) { 0L }
                            Pair(0L, apkSize)
                        }
                    } else {
                        val apkSize = try { java.io.File(appInfo.sourceDir).length() } catch (ex: Exception) { 0L }
                        Pair(0L, apkSize)
                    }

                    val appName = try { appInfo.loadLabel(packageManager).toString() } catch (e: Exception) { pkg.packageName }
                    
                    var icon = iconCache.get(pkg.packageName)
                    if (icon == null) {
                        icon = try { appInfo.loadIcon(packageManager) } catch (e: Exception) { null }
                        if (icon != null) {
                            iconCache.put(pkg.packageName, icon)
                        }
                    }

                    val isEnabled = appInfo.enabled
                    val lastUpdateTime = pkg.lastUpdateTime
                    val lastUsedTime = pkg.lastUpdateTime.takeIf { it > 0 } ?: installTime

                    AppCacheInfo(
                        packageName = pkg.packageName,
                        name = appName,
                        cacheBytes = cacheBytes,
                        cacheSizeString = formatSize(cacheBytes),
                        appSizeBytes = appSizeBytes,
                        appSizeString = formatSize(appSizeBytes),
                        icon = icon,
                        isSystemApp = isSystemApp,
                        isStopped = isStopped,
                        isStoppable = isStoppable,
                        isCleanable = isCleanable,
                        installTime = installTime,
                        isEnabled = isEnabled,
                        lastUpdateTime = lastUpdateTime,
                        lastUsedTime = lastUsedTime
                    )
                }
            }.awaitAll().filterNotNull()
        }

        val sortedList = appList.sortedBy { it.name.lowercase() }
        cachedAppsList = sortedList
        cachedPackageCount = packages.size
        sortedList
    }

    fun invalidateCache() {
        cachedAppsList = null
        cachedPackageCount = -1
    }

    fun clearAllMemoryCaches() {
        cachedAppsList = null
        cachedPackageCount = -1
        try {
            iconCache.evictAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        System.gc()
    }

    fun formatSize(bytes: Long): String {
        val (valueStr, unitStr) = formatSizeParts(bytes, maxDecimals = 2)
        return "$valueStr $unitStr"
    }

    fun formatSizeParts(bytes: Long, maxDecimals: Int = 2): Pair<String, String> {
        if (bytes <= 0) return Pair("0", "KB")
        val kb = bytes / 1024f
        val mb = kb / 1024f
        val gb = mb / 1024f
        val tb = gb / 1024f

        return when {
            tb >= 1.0f -> Pair(formatNumber(tb, maxDecimals), "TB")
            gb >= 1.0f -> Pair(formatNumber(gb, maxDecimals), "GB")
            mb >= 1.0f -> Pair(formatNumber(mb, maxDecimals), "MB")
            kb >= 1.0f -> Pair(formatNumber(kb, maxDecimals), "KB")
            else -> Pair(bytes.toString(), "B")
        }
    }

    private fun formatNumber(value: Float, maxDecimals: Int): String {
        if (maxDecimals == 1) {
            val roundedInt = kotlin.math.round(value)
            if (kotlin.math.abs(value - roundedInt) < 0.05f) {
                return String.format(java.util.Locale.US, "%.0f", value)
            }
            return String.format(java.util.Locale.US, "%.1f", value)
        } else {
            val roundedInt = kotlin.math.round(value)
            if (kotlin.math.abs(value - roundedInt) < 0.005f) {
                return String.format(java.util.Locale.US, "%.2f", value)
            }
            val roundedOneDecimal = kotlin.math.round(value * 10f) / 10f
            if (kotlin.math.abs(value - roundedOneDecimal) < 0.005f) {
                return String.format(java.util.Locale.US, "%.1f", value)
            }
            return String.format(java.util.Locale.US, "%.2f", value)
        }
    }

    fun getTotalStorageBytes(context: Context): Long {
        return try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
        } catch (e: Exception) {
            try {
                val path = android.os.Environment.getDataDirectory()
                val stat = android.os.StatFs(path.path)
                stat.blockCountLong * stat.blockSizeLong
            } catch (ex: Exception) {
                0L
            }
        }
    }

    fun getUsedStorageBytes(context: Context): Long {
        return try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val total = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            val free = try {
                val path = android.os.Environment.getDataDirectory()
                val stat = android.os.StatFs(path.path)
                stat.availableBlocksLong * stat.blockSizeLong
            } catch (ex: Exception) {
                0L
            }
            total - free
        } catch (e: Exception) {
            0L
        }
    }

    fun getRamInfo(context: Context): RamInfo {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val total = memoryInfo.totalMem
            val free = memoryInfo.availMem
            val used = (total - free).coerceAtLeast(0L)

            RamInfo(
                totalBytes = total,
                usedBytes = used,
                freeBytes = free,
                usedString = formatSize(used),
                freeString = formatSize(free),
                totalString = formatSize(total)
            )
        } catch (e: Exception) {
            RamInfo(
                totalBytes = 0L,
                usedBytes = 0L,
                freeBytes = 0L,
                usedString = "0 MB",
                freeString = "0 MB",
                totalString = "0 MB"
            )
        }
    }
}

