package com.tkno.apex.ui.main

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.tkno.apex.ui.component.BackButton
import com.tkno.apex.util.CleanHistoryEntry
import com.tkno.apex.util.HistoryManager
import com.tkno.apex.util.HistoryTimePeriod
import com.tkno.apex.util.StopHistoryEntry

enum class HistoryMode {
    CLEAN, STOP
}

@Composable
fun HistoryScreen(
    mode: HistoryMode,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val darkBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val accentBlue = Color(0xFF48AFFF)
    val stopOrange = Color(0xFFFF6D00)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    var cleanHistory by remember { mutableStateOf(HistoryManager.getCleanHistory(context)) }
    var stopHistory by remember { mutableStateOf(HistoryManager.getStopHistory(context)) }

    var selectedPeriod by remember { mutableStateOf<HistoryTimePeriod?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    fun refreshHistory() {
        cleanHistory = HistoryManager.getCleanHistory(context)
        stopHistory = HistoryManager.getStopHistory(context)
    }

    // App icon cache helper
    val packageManager = context.packageManager
    val iconCache = remember { mutableStateMapOf<String, Drawable?>() }

    fun getAppIcon(packageName: String): Drawable? {
        if (iconCache.containsKey(packageName)) return iconCache[packageName]
        return try {
            val drawable = packageManager.getApplicationIcon(packageName)
            iconCache[packageName] = drawable
            drawable
        } catch (e: Exception) {
            iconCache[packageName] = null
            null
        }
    }

    // Dynamic metrics calculations for Today
    val todayCleanEntries = remember(cleanHistory) {
        HistoryManager.filterCleanHistoryByPeriod(cleanHistory, HistoryTimePeriod.TODAY)
    }
    val todayCleanBytes = remember(todayCleanEntries) {
        todayCleanEntries.sumOf { it.bytesCleared }
    }

    val todayStopEntries = remember(stopHistory) {
        HistoryManager.filterStopHistoryByPeriod(stopHistory, HistoryTimePeriod.TODAY)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp)
        ) {
            // 1. Top Bar - Always Title "History"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onBackClick)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.history_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Header (Clean or Stop)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mode == HistoryMode.STOP) {
                    val stopUnit = stringResource(R.string.history_stop_unit)
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = stopUnit,
                            color = Color.Transparent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${todayStopEntries.size}",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stopUnit,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.history_today_stopped),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    val formattedToday = HistoryManager.formatBytes(todayCleanBytes)
                    val parts = formattedToday.split(" ")
                    val num = parts.getOrNull(0) ?: "0"
                    val unit = parts.getOrNull(1) ?: "MB"

                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = unit,
                            color = Color.Transparent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = num,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.history_today_cleared),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Filter Options Cards List (Today, Yesterday, Last 7 days, Last 30 days)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                HistoryPeriodItem(
                    title = stringResource(R.string.history_period_today),
                    subtitle = stringResource(R.string.history_period_tap_details),
                    onClick = { selectedPeriod = HistoryTimePeriod.TODAY }
                )
                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                HistoryPeriodItem(
                    title = stringResource(R.string.history_period_yesterday),
                    subtitle = stringResource(R.string.history_period_tap_details),
                    onClick = { selectedPeriod = HistoryTimePeriod.YESTERDAY }
                )
                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                HistoryPeriodItem(
                    title = stringResource(R.string.history_period_last_7_days),
                    subtitle = stringResource(R.string.history_period_tap_details),
                    onClick = { selectedPeriod = HistoryTimePeriod.LAST_7_DAYS }
                )
                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                HistoryPeriodItem(
                    title = stringResource(R.string.history_period_last_30_days),
                    subtitle = stringResource(R.string.history_period_tap_details),
                    onClick = { selectedPeriod = HistoryTimePeriod.LAST_30_DAYS }
                )
            }
        }

        // Bottom "Clear All" Pill Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val clearAllColor = if (mode == HistoryMode.STOP) stopOrange else accentBlue
            Button(
                onClick = { showClearConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(39.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = clearAllColor
                )
            ) {
                Text(
                    text = stringResource(R.string.history_clear_all),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Period Details Page (Dedicated Page view on top when a period is clicked)
        selectedPeriod?.let { period ->
            BackHandler {
                selectedPeriod = null
            }

            val periodTitle = when (period) {
                HistoryTimePeriod.TODAY -> stringResource(R.string.history_period_today)
                HistoryTimePeriod.YESTERDAY -> stringResource(R.string.history_period_yesterday)
                HistoryTimePeriod.LAST_7_DAYS -> stringResource(R.string.history_period_last_7_days)
                HistoryTimePeriod.LAST_30_DAYS -> stringResource(R.string.history_period_last_30_days)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(darkBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    // Top Bar for Period Page
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackButton(onClick = { selectedPeriod = null })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = periodTitle,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (mode == HistoryMode.CLEAN) {
                        val periodCleanEntries = remember(cleanHistory, period) {
                            HistoryManager.filterCleanHistoryByPeriod(cleanHistory, period)
                        }

                        val groupedByApp = remember(periodCleanEntries) {
                            periodCleanEntries.groupBy { it.packageName }.map { (pkg, entries) ->
                                val appName = entries.firstOrNull()?.appName ?: pkg
                                val totalBytes = entries.sumOf { it.bytesCleared }
                                val latestTimestamp = entries.maxOf { it.timestamp }
                                Triple(pkg, appName, totalBytes to latestTimestamp)
                            }.sortedByDescending { it.third.first }
                        }

                        val totalAppsCount = groupedByApp.size
                        val totalCacheBytes = groupedByApp.sumOf { it.third.first }
                        val totalCacheFormatted = HistoryManager.formatBytes(totalCacheBytes)

                        // Summary Sub-header (Full-width Edge-to-Edge with 0 side margins)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (totalAppsCount == 1)
                                    stringResource(R.string.history_period_summary_singular, totalAppsCount, totalCacheFormatted)
                                else
                                    stringResource(R.string.history_period_summary_plural, totalAppsCount, totalCacheFormatted),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Apps List
                        if (groupedByApp.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.history_no_cache_cleared_period),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(groupedByApp, key = { it.first }) { (pkg, appName, info) ->
                                    val (bytes, _) = info
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val icon = getAppIcon(pkg)
                                            if (icon != null) {
                                                Image(
                                                    bitmap = icon.toBitmap(54, 54).asImageBitmap(),
                                                    contentDescription = appName,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Android,
                                                    contentDescription = appName,
                                                    tint = Color(0xFF3DDC84),
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = appName,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = stringResource(R.string.history_item_cache_size, HistoryManager.formatBytes(bytes)),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                        HorizontalDivider(
                                            color = dividerColor,
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Stop History Period Detail View
                        val periodStopEntries = remember(stopHistory, period) {
                            HistoryManager.filterStopHistoryByPeriod(stopHistory, period)
                        }

                        val groupedByApp = remember(periodStopEntries) {
                            periodStopEntries.groupBy { it.packageName }.map { (pkg, entries) ->
                                val appName = entries.firstOrNull()?.appName ?: pkg
                                val count = entries.size
                                val latestTimestamp = entries.maxOf { it.timestamp }
                                Triple(pkg, appName, count to latestTimestamp)
                            }.sortedByDescending { it.third.first }
                        }

                        val totalAppsCount = groupedByApp.size
                        val totalStopsCount = periodStopEntries.size

                        // Summary Sub-header (Full-width Edge-to-Edge with 0 side margins)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (totalAppsCount == 1)
                                    stringResource(R.string.history_period_summary_stop_singular, totalAppsCount, totalStopsCount)
                                else
                                    stringResource(R.string.history_period_summary_stop_plural, totalAppsCount, totalStopsCount),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Apps List for Stop History
                        if (groupedByApp.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.history_no_apps_stopped_period),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(groupedByApp, key = { it.first }) { (pkg, appName, info) ->
                                    val (count, timestamp) = info
                                    StopHistoryRowItem(
                                        packageName = pkg,
                                        appName = appName,
                                        stopCount = count,
                                        latestTimestamp = timestamp,
                                        iconDrawable = getAppIcon(pkg),
                                        dividerColor = dividerColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clear History Confirmation Dialog
        if (showClearConfirmation) {
            AlertDialog(
                onDismissRequest = { showClearConfirmation = false },
                containerColor = cardBg,
                title = {
                    Text(
                        text = stringResource(R.string.clear_history),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (mode == HistoryMode.CLEAN) stringResource(R.string.history_dialog_clear_clean_msg) else stringResource(R.string.history_dialog_clear_stop_msg),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (mode == HistoryMode.CLEAN) {
                                HistoryManager.clearCleanHistory(context)
                            } else {
                                HistoryManager.clearStopHistory(context)
                            }
                            refreshHistory()
                            showClearConfirmation = false
                        }
                    ) {
                        Text(stringResource(R.string.history_clear_all), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmation = false }) {
                        Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@Composable
private fun HistoryPeriodItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StopHistoryRowItem(
    packageName: String,
    appName: String,
    stopCount: Int,
    latestTimestamp: Long,
    iconDrawable: Drawable?,
    dividerColor: Color
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconDrawable != null) {
                Image(
                    bitmap = iconDrawable.toBitmap(54, 54).asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = appName,
                    tint = Color(0xFF3DDC84),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = HistoryManager.formatTimestamp(latestTimestamp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = Color(0xFFFF6D00).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (stopCount == 1) stringResource(R.string.history_stop_count_singular, stopCount) else stringResource(R.string.history_stop_count_plural, stopCount),
                    color = Color(0xFFFF6D00),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        HorizontalDivider(
            color = dividerColor,
            thickness = 1.dp
        )
    }
}

