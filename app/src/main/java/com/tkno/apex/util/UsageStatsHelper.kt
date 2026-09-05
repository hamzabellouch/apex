package com.tkno.apex.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tkno.apex.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AppCategoryType(
    @StringRes val titleRes: Int,
    val badgeColor: Color,
    val iconVector: ImageVector
) {
    OTHER(
        titleRes = R.string.category_other,
        badgeColor = Color(0xFF00C49F), // Cyan / Teal
        iconVector = Icons.Default.Category
    ),
    SOCIAL(
        titleRes = R.string.category_social,
        badgeColor = Color(0xFF3370FF), // Vibrant Blue
        iconVector = Icons.Default.Favorite
    ),
    PRODUCTIVITY(
        titleRes = R.string.category_productivity_finance,
        badgeColor = Color(0xFF8A56FF), // Purple
        iconVector = Icons.Default.Work
    ),
    ENTERTAINMENT(
        titleRes = R.string.category_entertainment,
        badgeColor = Color(0xFFFF5252), // Coral / Red
        iconVector = Icons.Default.Movie
    ),
    GAMES(
        titleRes = R.string.category_games,
        badgeColor = Color(0xFF27AE60), // Green
        iconVector = Icons.Default.Gamepad
    ),
    TOOLS(
        titleRes = R.string.category_tools,
        badgeColor = Color(0xFFFFA000), // Orange
        iconVector = Icons.Default.Build
    )
}

data class DayUsage(
    val dayIndex: Int, // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
    val dayLetter: String, // "S", "M", "T", "W", "T", "F", "S"
    val dayFullName: String,
    val dateMillis: Long,
    val totalTimeMillis: Long,
    val formattedTime: String
)

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val totalTimeMillis: Long,
    val formattedTime: String,
    val categoryType: AppCategoryType
)

data class CategoryUsageGroup(
    val categoryType: AppCategoryType,
    val totalTimeMillis: Long,
    val formattedTotalTime: String,
    val previousAvgTimeMillis: Long,
    val formattedPreviousAvgTime: String,
    val apps: List<AppUsageItem>
)

data class CategorySegment(
    val categoryType: AppCategoryType,
    val totalTimeMillis: Long,
    val formattedTime: String,
    val fraction: Float // 0f to 1f
)

data class TodayUsageStats(
    val totalTimeMillis: Long,
    val formattedTotalTime: String,
    val categorySegments: List<CategorySegment>,
    val mostUsedApps: List<AppUsageItem>
)

data class WeekUsageStats(
    val weekOffset: Int, // 0 = current week, -1 = last week, etc.
    val weekStartMillis: Long,
    val weekEndMillis: Long,
    val weekNumber: Int,
    val formattedDateRange: String,
    val dailyUsages: List<DayUsage>,
    val dailyAverageMillis: Long,
    val formattedDailyAverage: String,
    val totalWeekMillis: Long,
    val categoryGroups: List<CategoryUsageGroup>,
    val topApps: List<AppUsageItem>
)

object UsageStatsHelper {

    private val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

    /**
     * Formats milliseconds into human-readable duration:
     * e.g. "7 h 54 m", "45 m", "12 s"
     */
    fun formatDuration(timeMillis: Long): String {
        if (timeMillis <= 0) return "0 m"
        val totalSeconds = timeMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> {
                if (minutes > 0) "$hours h $minutes m" else "$hours h"
            }
            minutes > 0 -> "$minutes m"
            else -> "$seconds s"
        }
    }

    /**
     * Computes the start and end of the week for a given weekOffset (0 = current week, -1 = previous week)
     */
    fun getWeekTimeBounds(weekOffset: Int = 0): Pair<Calendar, Calendar> {
        val startCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }

        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return Pair(startCal, endCal)
    }

    /**
     * Formats date range like "August 23 - August 29 (Week 35)"
     */
    fun formatDateRangeHeader(startCal: Calendar, endCal: Calendar): String {
        val monthFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
        val sameMonth = startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
        val endFormat = if (sameMonth) {
            SimpleDateFormat("d", Locale.getDefault())
        } else {
            SimpleDateFormat("MMMM d", Locale.getDefault())
        }

        val startStr = monthFormat.format(startCal.time)
        val endStr = endFormat.format(endCal.time)
        val weekNumber = startCal.get(Calendar.WEEK_OF_YEAR)

        return "$startStr - $endStr (Week $weekNumber)"
    }

    fun detectCategory(pkg: String, appInfo: ApplicationInfo?): AppCategoryType {
        val lower = pkg.lowercase()

        // Known Social Apps
        if (lower.contains("instagram") ||
            lower.contains("tiktok") ||
            lower.contains("facebook") ||
            lower.contains("katana") ||
            lower.contains("orca") ||
            lower.contains("whatsapp") ||
            lower.contains("telegram") ||
            lower.contains("twitter") ||
            lower.contains("snapchat") ||
            lower.contains("reddit") ||
            lower.contains("discord") ||
            lower.contains("pinterest") ||
            lower.contains("linkedin") ||
            lower.contains("barcelona") ||
            lower.contains("threads") ||
            lower.contains("signal") ||
            lower.contains("viber") ||
            lower.contains("wechat") ||
            lower.contains("social")
        ) {
            return AppCategoryType.SOCIAL
        }

        // Known Productivity & Finance
        if (lower.contains("chrome") ||
            lower.contains("browser") ||
            lower.contains("gmail") ||
            lower.contains("docs") ||
            lower.contains("sheets") ||
            lower.contains("slides") ||
            lower.contains("office") ||
            lower.contains("notes") ||
            lower.contains("calendar") ||
            lower.contains("calculator") ||
            lower.contains("clock") ||
            lower.contains("slack") ||
            lower.contains("zoom") ||
            lower.contains("github") ||
            lower.contains("bank") ||
            lower.contains("pay") ||
            lower.contains("wallet") ||
            lower.contains("finance")
        ) {
            return AppCategoryType.PRODUCTIVITY
        }

        // Known Entertainment Apps
        if (lower.contains("youtube") ||
            lower.contains("netflix") ||
            lower.contains("spotify") ||
            lower.contains("twitch") ||
            lower.contains("disney") ||
            lower.contains("amazon.avod") ||
            lower.contains("music") ||
            lower.contains("video") ||
            lower.contains("player") ||
            lower.contains("tv") ||
            lower.contains("soundcloud") ||
            lower.contains("media")
        ) {
            return AppCategoryType.ENTERTAINMENT
        }

        // Known Games
        if (lower.contains("game") ||
            lower.contains("supercell") ||
            lower.contains("roblox") ||
            lower.contains("pubg") ||
            lower.contains("mojang") ||
            lower.contains("candycrush") ||
            lower.contains("unity") ||
            lower.contains("ea.gp") ||
            lower.contains("gameloft")
        ) {
            return AppCategoryType.GAMES
        }

        // Android ApplicationInfo Category Fallback (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategoryType.SOCIAL
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategoryType.ENTERTAINMENT
                ApplicationInfo.CATEGORY_GAME -> return AppCategoryType.GAMES
                ApplicationInfo.CATEGORY_PRODUCTIVITY,
                ApplicationInfo.CATEGORY_NEWS -> return AppCategoryType.PRODUCTIVITY
                ApplicationInfo.CATEGORY_MAPS -> return AppCategoryType.TOOLS
            }
        }

        return AppCategoryType.OTHER
    }

    /**
     * Loads Today's Screen Time, Category progress segments, and Most Used Apps
     */
    suspend fun getTodayUsageStats(context: Context): TodayUsageStats = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val usageStatsManager = try {
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        } catch (e: Exception) {
            null
        }

        val selfPackage = context.packageName

        val todayStartCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayEndCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        if (usageStatsManager == null) {
            return@withContext TodayUsageStats(
                totalTimeMillis = 0L,
                formattedTotalTime = "0 m",
                categorySegments = emptyList(),
                mostUsedApps = emptyList()
            )
        }

        val todayStatsMap = try {
            usageStatsManager.queryAndAggregateUsageStats(todayStartCal.timeInMillis, todayEndCal.timeInMillis)
        } catch (e: Exception) {
            emptyMap<String, UsageStats>()
        }

        val appUsageList = mutableListOf<AppUsageItem>()
        var totalTodayMillis = 0L

        for ((pkg, stats) in todayStatsMap) {
            if (pkg == selfPackage || stats.totalTimeInForeground < 2000L) continue

            totalTodayMillis += stats.totalTimeInForeground

            val appInfo = try {
                packageManager.getApplicationInfo(pkg, 0)
            } catch (e: Exception) {
                null
            }

            val appName = try {
                appInfo?.loadLabel(packageManager)?.toString() ?: pkg
            } catch (e: Exception) {
                pkg
            }

            val icon = try {
                appInfo?.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val category = detectCategory(pkg, appInfo)

            appUsageList.add(
                AppUsageItem(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    totalTimeMillis = stats.totalTimeInForeground,
                    formattedTime = formatDuration(stats.totalTimeInForeground),
                    categoryType = category
                )
            )
        }

        val sortedApps = appUsageList.sortedByDescending { it.totalTimeMillis }
        val mostUsedApps = sortedApps.take(3)

        // Group by category and compute segments
        val groupedByCategory = sortedApps.groupBy { it.categoryType }
        val segments = mutableListOf<CategorySegment>()

        for ((category, apps) in groupedByCategory) {
            val catTotalTime = apps.sumOf { it.totalTimeMillis }
            val fraction = if (totalTodayMillis > 0) {
                (catTotalTime.toFloat() / totalTodayMillis).coerceIn(0f, 1f)
            } else {
                0f
            }

            segments.add(
                CategorySegment(
                    categoryType = category,
                    totalTimeMillis = catTotalTime,
                    formattedTime = formatDuration(catTotalTime),
                    fraction = fraction
                )
            )
        }

        val sortedSegments = segments.sortedByDescending { it.totalTimeMillis }

        TodayUsageStats(
            totalTimeMillis = totalTodayMillis,
            formattedTotalTime = formatDuration(totalTodayMillis),
            categorySegments = sortedSegments,
            mostUsedApps = mostUsedApps
        )
    }

    /**
     * Loads the entire weekly usage stats, breakdown, and historical comparison.
     */
    suspend fun getWeekUsageStats(
        context: Context,
        weekOffset: Int = 0
    ): WeekUsageStats = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val usageStatsManager = try {
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        } catch (e: Exception) {
            null
        }

        val (startCal, endCal) = getWeekTimeBounds(weekOffset)
        val weekStartMillis = startCal.timeInMillis
        val weekEndMillis = endCal.timeInMillis
        val weekNumber = startCal.get(Calendar.WEEK_OF_YEAR)
        val formattedDateRange = formatDateRangeHeader(startCal, endCal)

        if (usageStatsManager == null) {
            return@withContext createEmptyWeekStats(weekOffset, weekStartMillis, weekEndMillis, weekNumber, formattedDateRange)
        }

        val selfPackage = context.packageName

        // 1. Calculate Daily Usage for the 7 days (Sunday - Saturday)
        val dailyUsages = mutableListOf<DayUsage>()
        var weekTotalMillis = 0L

        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        for (i in 0..6) {
            val dayStartCal = (startCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_WEEK, i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayEndCal = (dayStartCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val dayStatsMap = try {
                usageStatsManager.queryAndAggregateUsageStats(dayStartCal.timeInMillis, dayEndCal.timeInMillis)
            } catch (e: Exception) {
                emptyMap<String, UsageStats>()
            }

            var dayTotalMillis = 0L
            for ((pkg, stats) in dayStatsMap) {
                if (pkg != selfPackage && stats.totalTimeInForeground > 1000L) {
                    dayTotalMillis += stats.totalTimeInForeground
                }
            }

            weekTotalMillis += dayTotalMillis

            dailyUsages.add(
                DayUsage(
                    dayIndex = i,
                    dayLetter = dayLetters.getOrElse(i) { "" },
                    dayFullName = dayNames.getOrElse(i) { "" },
                    dateMillis = dayStartCal.timeInMillis,
                    totalTimeMillis = dayTotalMillis,
                    formattedTime = formatDuration(dayTotalMillis)
                )
            )
        }

        val currentCal = Calendar.getInstance()
        val daysForAverage = if (weekOffset == 0) {
            val todayDayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK) - 1
            (todayDayOfWeek + 1).coerceIn(1, 7)
        } else {
            7
        }

        val dailyAverageMillis = if (daysForAverage > 0) weekTotalMillis / daysForAverage else 0L
        val formattedDailyAverage = formatDuration(dailyAverageMillis)

        // 2. Query Aggregate stats for the whole selected week
        val weekStatsMap = try {
            usageStatsManager.queryAndAggregateUsageStats(weekStartMillis, weekEndMillis)
        } catch (e: Exception) {
            emptyMap<String, UsageStats>()
        }

        // 3. Query previous 3 weeks stats for comparison
        val prev3WeeksStartCal = (startCal.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, -3)
        }
        val prev3WeeksStatsMap = try {
            usageStatsManager.queryAndAggregateUsageStats(prev3WeeksStartCal.timeInMillis, weekStartMillis)
        } catch (e: Exception) {
            emptyMap<String, UsageStats>()
        }

        val appUsageList = mutableListOf<AppUsageItem>()

        for ((pkg, stats) in weekStatsMap) {
            if (pkg == selfPackage || stats.totalTimeInForeground < 5000L) continue

            val appInfo = try {
                packageManager.getApplicationInfo(pkg, 0)
            } catch (e: Exception) {
                null
            }

            val appName = try {
                appInfo?.loadLabel(packageManager)?.toString() ?: pkg
            } catch (e: Exception) {
                pkg
            }

            val icon = try {
                appInfo?.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val category = detectCategory(pkg, appInfo)

            appUsageList.add(
                AppUsageItem(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    totalTimeMillis = stats.totalTimeInForeground,
                    formattedTime = formatDuration(stats.totalTimeInForeground),
                    categoryType = category
                )
            )
        }

        val sortedApps = appUsageList.sortedByDescending { it.totalTimeMillis }
        val groupedByCategory = sortedApps.groupBy { it.categoryType }
        val categoryGroups = mutableListOf<CategoryUsageGroup>()

        val prevCategoryTotals = mutableMapOf<AppCategoryType, Long>()
        for ((pkg, stats) in prev3WeeksStatsMap) {
            if (pkg == selfPackage) continue
            val appInfo = try { packageManager.getApplicationInfo(pkg, 0) } catch (e: Exception) { null }
            val cat = detectCategory(pkg, appInfo)
            val current = prevCategoryTotals.getOrDefault(cat, 0L)
            prevCategoryTotals[cat] = current + stats.totalTimeInForeground
        }

        for ((category, apps) in groupedByCategory) {
            val totalCatTime = apps.sumOf { it.totalTimeMillis }
            val prev3WeeksTotal = prevCategoryTotals.getOrDefault(category, 0L)
            val prevAvgTime = prev3WeeksTotal / 3

            categoryGroups.add(
                CategoryUsageGroup(
                    categoryType = category,
                    totalTimeMillis = totalCatTime,
                    formattedTotalTime = formatDuration(totalCatTime),
                    previousAvgTimeMillis = prevAvgTime,
                    formattedPreviousAvgTime = formatDuration(prevAvgTime),
                    apps = apps.sortedByDescending { it.totalTimeMillis }
                )
            )
        }

        val sortedCategoryGroups = categoryGroups.sortedByDescending { it.totalTimeMillis }

        WeekUsageStats(
            weekOffset = weekOffset,
            weekStartMillis = weekStartMillis,
            weekEndMillis = weekEndMillis,
            weekNumber = weekNumber,
            formattedDateRange = formattedDateRange,
            dailyUsages = dailyUsages,
            dailyAverageMillis = dailyAverageMillis,
            formattedDailyAverage = formattedDailyAverage,
            totalWeekMillis = weekTotalMillis,
            categoryGroups = sortedCategoryGroups,
            topApps = sortedApps
        )
    }

    private fun createEmptyWeekStats(
        weekOffset: Int,
        weekStartMillis: Long,
        weekEndMillis: Long,
        weekNumber: Int,
        formattedDateRange: String
    ): WeekUsageStats {
        val dailyUsages = (0..6).map { i ->
            DayUsage(
                dayIndex = i,
                dayLetter = dayLetters.getOrElse(i) { "" },
                dayFullName = "",
                dateMillis = weekStartMillis + (i * 86400000L),
                totalTimeMillis = 0L,
                formattedTime = "0 m"
            )
        }

        return WeekUsageStats(
            weekOffset = weekOffset,
            weekStartMillis = weekStartMillis,
            weekEndMillis = weekEndMillis,
            weekNumber = weekNumber,
            formattedDateRange = formattedDateRange,
            dailyUsages = dailyUsages,
            dailyAverageMillis = 0L,
            formattedDailyAverage = "0 m",
            totalWeekMillis = 0L,
            categoryGroups = emptyList(),
            topApps = emptyList()
        )
    }
}
