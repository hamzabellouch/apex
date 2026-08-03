package com.tkno.blueiris.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.tkno.blueiris.R
import com.tkno.blueiris.util.AppStorageHelper

@Composable
fun CleanScreen(
    totalStorageBytes: Long,
    usedStorageBytes: Long,
    totalCacheBytes: Long,
    onOpenWhitelist: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingCacheMb = (totalCacheBytes / (1024 * 1024)).toInt()
    val isCleaned = totalCacheBytes == 0L

    val darkBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val accentBlue = Color(0xFF48AFFF)
    val mintGreen = Color(0xFF2EC4B6)

    var showStorageDetailsDialog by remember { mutableStateOf(false) }
    var showLiveCacheDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.clean_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onOpenHistory) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(id = R.string.clean_history_icon_desc),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Dynamic gauge target calculation based on max 1 GB cache capacity
            val maxCacheBytes = 1024f * 1024f * 1024f // 1 GB
            val targetFraction = remember(totalCacheBytes, isCleaned) {
                if (isCleaned || totalCacheBytes == 0L) 0.0f
                else (totalCacheBytes.toFloat() / maxCacheBytes).coerceIn(0.02f, 1.0f)
            }
            val progressAnimatable = remember { Animatable(0f) }

            // Re-trigger animation from 0 to target value whenever screen is opened or totalCacheBytes updates
            LaunchedEffect(totalCacheBytes, isCleaned) {
                progressAnimatable.snapTo(0f)
                progressAnimatable.animateTo(
                    targetValue = targetFraction,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }

            val animatedRatio = if (targetFraction > 0f) (progressAnimatable.value / targetFraction).coerceIn(0f, 1f) else 0f
            val animatedMb = (remainingCacheMb * animatedRatio).toInt()
            val displayMbStr = if (totalCacheBytes > 0 && remainingCacheMb == 0) "1" else animatedMb.toString()

            val gaugeTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            // Circular Gauge Card for Clean
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2 * 0.85f
                    val center = Offset(size.width / 2, size.height / 2 - 10.dp.toPx())

                    // Track Arc
                    drawArc(
                        color = gaugeTrackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Fill Arc (Dynamic animation starting from 0 upon tab entry)
                    drawArc(
                        color = if (isCleaned) mintGreen else accentBlue,
                        startAngle = 135f,
                        sweepAngle = 270f * progressAnimatable.value,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Gauge Text Inside
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.offset(y = (-10).dp)
                ) {
                    Text(
                        text = if (isCleaned) "0" else displayMbStr,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(id = R.string.clean_gauge_unit_mb),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isCleaned) stringResource(id = R.string.clean_gauge_system_cleaned) else stringResource(id = R.string.clean_gauge_can_be_cleared),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                // Percentage Labels
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.clean_gauge_zero_mb),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 16.dp, y = (-24).dp)
                    )
                    Text(
                        text = stringResource(id = R.string.clean_gauge_current_mb, if (isCleaned) "0" else displayMbStr),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-16).dp, y = (-24).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Row (Total, Used, Cache) - Matching StopStatItem styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CleanStatItem(
                    color = Color(0xFF2EC4B6),
                    title = stringResource(id = R.string.clean_stat_total),
                    value = AppStorageHelper.formatSize(totalStorageBytes),
                    modifier = Modifier.weight(1f)
                )
                CleanStatItem(
                    color = Color(0xFF3B82F6),
                    title = stringResource(id = R.string.clean_stat_used),
                    value = AppStorageHelper.formatSize(usedStorageBytes),
                    modifier = Modifier.weight(1f)
                )
                CleanStatItem(
                    color = Color(0xFFEC4899),
                    title = stringResource(id = R.string.clean_stat_cache),
                    value = AppStorageHelper.formatSize(totalCacheBytes),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Row - Matching StopActionCircleButton styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CleanActionCircleButton(
                    icon = Icons.Default.Storage,
                    label = stringResource(id = R.string.clean_action_storage_details),
                    onClick = { showStorageDetailsDialog = true },
                    modifier = Modifier.weight(1f)
                )
                CleanActionCircleButton(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    label = stringResource(id = R.string.clean_action_whitelist),
                    onClick = onOpenWhitelist,
                    modifier = Modifier.weight(1f)
                )
                CleanActionCircleButton(
                    icon = Icons.Default.Sync,
                    label = stringResource(id = R.string.clean_action_live_cache),
                    onClick = { showLiveCacheDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Storage Details Dialog
    if (showStorageDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDetailsDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.clean_dialog_storage_details_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.clean_dialog_total_storage, AppStorageHelper.formatSize(totalStorageBytes)),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(id = R.string.clean_dialog_used_storage, AppStorageHelper.formatSize(usedStorageBytes)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(id = R.string.clean_dialog_clearable_cache, AppStorageHelper.formatSize(totalCacheBytes)),
                        color = accentBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = { showStorageDetailsDialog = false }) {
                    Text(text = stringResource(id = R.string.clean_dialog_close), color = accentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Live Cache Dialog
    if (showLiveCacheDialog) {
        AlertDialog(
            onDismissRequest = { showLiveCacheDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.clean_dialog_live_cache_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.clean_dialog_live_cache_desc, AppStorageHelper.formatSize(totalCacheBytes)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = { showLiveCacheDialog = false }) {
                    Text(text = stringResource(id = R.string.clean_dialog_ok), color = accentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun CleanStatItem(
    color: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CleanActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
