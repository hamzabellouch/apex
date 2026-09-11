package com.tkno.apex.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tkno.apex.R
import com.tkno.apex.ui.icon.DigitalWellbeing
import com.tkno.apex.ui.icon.LeftPanelOpen
import com.tkno.apex.util.AppUsageItem
import com.tkno.apex.util.CategorySegment
import com.tkno.apex.util.CategoryUsageGroup
import com.tkno.apex.util.DayUsage
import com.tkno.apex.util.TodayUsageStats
import com.tkno.apex.util.UsageStatsHelper
import com.tkno.apex.util.WeekUsageStats

@Composable
fun StatisticsScreen(
    onOpenDrawer: () -> Unit = {},
    showMenuButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isDetailedViewActive by remember { mutableStateOf(false) }

    var todayStats by remember { mutableStateOf<TodayUsageStats?>(null) }
    var isTodayLoading by remember { mutableStateOf(true) }

    var weekOffset by remember { mutableIntStateOf(0) }
    var weekStats by remember { mutableStateOf<WeekUsageStats?>(null) }
    var isWeekLoading by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    // Intercept system back press when in detailed view
    BackHandler(enabled = isDetailedViewActive) {
        isDetailedViewActive = false
    }

    // Load Today Stats on first compose
    LaunchedEffect(Unit) {
        isTodayLoading = true
        todayStats = UsageStatsHelper.getTodayUsageStats(context)
        isTodayLoading = false
    }

    // Load Week Stats when detailed view is opened or weekOffset changes
    LaunchedEffect(isDetailedViewActive, weekOffset) {
        if (isDetailedViewActive) {
            isWeekLoading = true
            selectedDayIndex = null
            weekStats = UsageStatsHelper.getWeekUsageStats(context, weekOffset)
            isWeekLoading = false
        }
    }

    val darkBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        AnimatedContent(
            targetState = isDetailedViewActive,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(200)))
                } else {
                    (slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)))
                }
            },
            label = "StatisticsViewTransition"
        ) { inDetail ->
            if (!inDetail) {
                // ==========================================
                // 1. DEFAULT OVERVIEW SCREEN (Today's Usage)
                // ==========================================
                StatisticsOverviewView(
                    todayStats = todayStats,
                    isLoading = isTodayLoading,
                    showMenuButton = showMenuButton,
                    onOpenDrawer = onOpenDrawer,
                    onOpenDetailedView = { isDetailedViewActive = true }
                )
            } else {
                // ==========================================
                // 2. DETAILED WEEKLY STATISTICS SCREEN
                // ==========================================
                StatisticsDetailedWeeklyView(
                    weekStats = weekStats,
                    weekOffset = weekOffset,
                    isLoading = isWeekLoading,
                    selectedDayIndex = selectedDayIndex,
                    onWeekOffsetChange = { weekOffset = it },
                    onDayClick = { clickedIndex ->
                        selectedDayIndex = if (selectedDayIndex == clickedIndex) null else clickedIndex
                    },
                    onNavigateBack = { isDetailedViewActive = false }
                )
            }
        }
    }
}

// =========================================================================
// OVERVIEW SCREEN: Today Screen Time, Segmented Progress, Most Used Apps
// =========================================================================

@Composable
private fun StatisticsOverviewView(
    todayStats: TodayUsageStats?,
    isLoading: Boolean,
    showMenuButton: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenDetailedView: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Bar: Menu Button / Title + Top-Right Chart & More Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (showMenuButton) Modifier.offset(x = (-12).dp) else Modifier
            ) {
                if (showMenuButton) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = LeftPanelOpen,
                            contentDescription = stringResource(id = R.string.nav_menu),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.nav_statistics),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Action Icons: Bar Chart Icon (Opens Detailed view) + More Vert Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenDetailedView) {
                    Icon(
                        imageVector = DigitalWellbeing,
                        contentDescription = stringResource(R.string.detailed_statistics),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.menu),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (isLoading && todayStats == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF48AFFF),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            val stats = todayStats ?: TodayUsageStats(0L, "0 m", emptyList(), emptyList())

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Card 1: Today Screen Time & Segmented Color Progress Bar
                item {
                    TodayScreenTimeCard(stats = stats)
                }

                // Card 2: Most Used Apps
                item {
                    MostUsedAppsCard(apps = stats.mostUsedApps)
                }
            }
        }
    }
}

/**
 * Top Card on Overview Screen: Big time, segmented color bar, category legend list
 */
@Composable
private fun TodayScreenTimeCard(stats: TodayUsageStats) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Big Headline Metric: e.g. "7 h 54 m"
            Text(
                text = stats.formattedTotalTime,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Multi-segment horizontal color progress bar
            SegmentedProgressBar(
                segments = stats.categorySegments,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Categories list with colored dots
            if (stats.categorySegments.isNotEmpty()) {
                stats.categorySegments.forEachIndexed { index, segment ->
                    CategoryLegendRow(segment = segment)
                    if (index < stats.categorySegments.lastIndex) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_usage_data_today),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Segmented Color Bar representing categories usage proportion
 */
@Composable
private fun SegmentedProgressBar(
    segments: List<CategorySegment>,
    modifier: Modifier = Modifier
) {
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(fallbackColor)
    ) {
        if (segments.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxSize()) {
                segments.forEach { segment ->
                    val weight = segment.fraction.coerceAtLeast(0.01f)
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(segment.categoryType.badgeColor)
                    )
                }
            }
        }
    }
}

/**
 * Category Row with color dot, category name, and usage duration
 */
@Composable
private fun CategoryLegendRow(segment: CategorySegment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored circle indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(segment.categoryType.badgeColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Category Name
        Text(
            text = stringResource(segment.categoryType.titleRes),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Usage Duration
        Text(
            text = segment.formattedTime,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Bottom Card: "Most used apps" grid
 */
@Composable
private fun MostUsedAppsCard(apps: List<AppUsageItem>) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = stringResource(R.string.most_used_apps),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (apps.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Top
                ) {
                    apps.forEach { app ->
                        MostUsedAppGridItem(
                            app = app,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_usage_data_today),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Individual App Column inside "Most used apps" card
 */
@Composable
private fun MostUsedAppGridItem(
    app: AppUsageItem,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(app.icon) {
        try {
            app.icon?.toBitmap()?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = app.appName,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = app.appName,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App Name
        Text(
            text = app.appName,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Usage Duration
        Text(
            text = app.formattedTime,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

// =========================================================================
// DETAILED WEEKLY STATISTICS SCREEN: Weekly Chart & Category Breakdown Cards
// =========================================================================

@Composable
private fun StatisticsDetailedWeeklyView(
    weekStats: WeekUsageStats?,
    weekOffset: Int,
    isLoading: Boolean,
    selectedDayIndex: Int?,
    onWeekOffsetChange: (Int) -> Unit,
    onDayClick: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Bar: Back Button + Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(id = R.string.nav_statistics),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = (-8).dp)
            )
        }

        // Week Navigator (Date range header + Left/Right arrows)
        weekStats?.let { stats ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onWeekOffsetChange(weekOffset - 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.previous_week),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = stats.formattedDateRange,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        if (weekOffset < 0) onWeekOffsetChange(weekOffset + 1)
                    },
                    enabled = weekOffset < 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.next_week),
                        tint = if (weekOffset < 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isLoading && weekStats == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF48AFFF),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            weekStats?.let { stats ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Top Hero Card: Screen Time Metric & Weekly Bar Chart
                    item {
                        WeeklyScreenTimeCard(
                            stats = stats,
                            selectedDayIndex = selectedDayIndex,
                            onDayClick = onDayClick
                        )
                    }

                    // Category Breakdown Cards
                    if (stats.categoryGroups.isNotEmpty()) {
                        items(
                            items = stats.categoryGroups,
                            key = { it.categoryType.name }
                        ) { group ->
                            CategoryBreakdownCard(group = group)
                        }
                    } else {
                        item {
                            EmptyUsageCard()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Card containing the big average screen time number and the custom weekly bar chart
 */
@Composable
private fun WeeklyScreenTimeCard(
    stats: WeekUsageStats,
    selectedDayIndex: Int?,
    onDayClick: (Int) -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val primaryBlue = Color(0xFF389BF2)
    val averageDashedColor = Color(0xFFFF5252)

    val selectedDay = selectedDayIndex?.let { stats.dailyUsages.getOrNull(it) }

    val heroMetricText = selectedDay?.formattedTime ?: stats.formattedDailyAverage
    val heroSubtitleText = if (selectedDay != null) {
        stringResource(R.string.screen_time_for_day, selectedDay.dayFullName)
    } else {
        stringResource(R.string.daily_avg_screen_time)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Hero Large Number
            AnimatedContent(
                targetState = heroMetricText,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "HeroMetricAnim"
            ) { text ->
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle
            AnimatedContent(
                targetState = heroSubtitleText,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "HeroSubtitleAnim"
            ) { text ->
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart with Dashed Average Line and Right Y-Axis Labels
            WeeklyBarChartView(
                dailyUsages = stats.dailyUsages,
                dailyAverageMillis = stats.dailyAverageMillis,
                selectedDayIndex = selectedDayIndex,
                onDayClick = onDayClick,
                barColor = primaryBlue,
                averageLineColor = averageDashedColor
            )
        }
    }
}

/**
 * Custom Compose Bar Chart with 7 vertical pill bars, dashed average line, and dynamic Y-axis markers
 */
@Composable
private fun WeeklyBarChartView(
    dailyUsages: List<DayUsage>,
    dailyAverageMillis: Long,
    selectedDayIndex: Int?,
    onDayClick: (Int) -> Unit,
    barColor: Color,
    averageLineColor: Color
) {
    val maxDayMillis = (dailyUsages.maxOfOrNull { it.totalTimeMillis } ?: 0L).coerceAtLeast(1L)

    val maxHours = (maxDayMillis / (1000 * 60 * 60f)).coerceAtLeast(1f)
    val yMaxHours = when {
        maxHours > 16f -> 24f
        maxHours > 12f -> 18f
        maxHours > 8f -> 12f
        maxHours > 4f -> 8f
        else -> 4f
    }
    val yAxisMaxMillis = (yMaxHours * 3600 * 1000).toLong()

    val label1Hours = (yMaxHours).toInt()
    val label2Hours = (yMaxHours * 2 / 3).toInt()
    val label3Hours = (yMaxHours / 3).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Left side: Chart area (Bars + Dashed Average Line)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // 1. Canvas layer: Dashed horizontal average line
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 28.dp, top = 8.dp)
            ) {
                if (dailyAverageMillis > 0 && yAxisMaxMillis > 0) {
                    val avgRatio = (dailyAverageMillis.toFloat() / yAxisMaxMillis).coerceIn(0f, 1f)
                    val avgY = size.height * (1f - avgRatio)

                    drawLine(
                        color = averageLineColor,
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }
            }

            // 2. Interactive Bars Column Row
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyUsages.forEachIndexed { index, day ->
                    val isSelected = (selectedDayIndex == index)
                    val targetFraction = if (yAxisMaxMillis > 0) {
                        (day.totalTimeMillis.toFloat() / yAxisMaxMillis).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    val animatedFraction by animateFloatAsState(
                        targetValue = targetFraction,
                        animationSpec = tween(durationMillis = 400),
                        label = "BarHeightAnim_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDayClick(index) }
                    ) {
                        // Vertical Bar Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Background track
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color.Transparent)
                            )

                            // Filled Bar (blue rounded capsule)
                            val barHeightFactor = animatedFraction.coerceAtLeast(if (day.totalTimeMillis > 0) 0.04f else 0f)
                            val activeBarColor = if (isSelected) Color(0xFF64B5F6) else barColor

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .fillMaxHeight(barHeightFactor)
                                    .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(activeBarColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Day letter (S, M, T, W, T, F, S)
                        Text(
                            text = day.dayLetter,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side: Y-Axis time labels (e.g. 12 h, 8 h, 4 h)
        Column(
            modifier = Modifier
                .width(34.dp)
                .fillMaxHeight()
                .padding(bottom = 28.dp, top = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$label1Hours h",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$label2Hours h",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$label3Hours h",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Category Breakdown Card (e.g. Social, Entertainment, Games, etc.)
 */
@Composable
private fun CategoryBreakdownCard(
    group: CategoryUsageGroup
) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Category Title + Top-Right Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(group.categoryType.titleRes),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Category Icon Badge (e.g. circle with category icon)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(group.categoryType.badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = group.categoryType.iconVector,
                        contentDescription = stringResource(group.categoryType.titleRes),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Category Total Time
            Text(
                text = group.formattedTotalTime,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Previous 3 weeks average comparison
            val comparisonText = if (group.previousAvgTimeMillis > 0) {
                stringResource(R.string.used_avg_prev_weeks, group.formattedPreviousAvgTime)
            } else {
                group.formattedTotalTime
            }

            Text(
                text = comparisonText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Apps List
            group.apps.forEach { app ->
                AppUsageRowItem(app = app)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Individual App Row inside Category Card (Icon, Name, Usage Duration)
 */
@Composable
private fun AppUsageRowItem(
    app: AppUsageItem
) {
    val bitmap = remember(app.icon) {
        try {
            app.icon?.toBitmap()?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = app.appName,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = app.appName,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // App Name
        Text(
            text = app.appName,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // App Usage Time
        Text(
            text = app.formattedTime,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Empty Card when no usage data is recorded for the week
 */
@Composable
private fun EmptyUsageCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QueryStats,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_usage_data_week),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
