package com.tkno.apex.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.tkno.apex.R
import com.tkno.apex.model.AppCacheInfo
import com.tkno.apex.util.RamInfo

@Composable
fun StopScreen(
    installedApps: List<AppCacheInfo>,
    ramInfo: RamInfo,
    onOpenWhitelist: () -> Unit,
    onAnalyzeStopClick: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val darkBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val stopOrange = Color(0xFFFF6D00)
    val appsCount = installedApps.size

    var showRunningAppsDialog by remember { mutableStateOf(false) }
    var showMemoryInfoDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(id = R.string.stop_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onOpenHistory) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(id = R.string.stop_history_icon_desc),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Dynamic gauge target calculation based on max 50 apps capacity
            val targetFraction = remember(appsCount) {
                if (appsCount == 0) 0.0f else (appsCount.toFloat() / 50f).coerceIn(0.02f, 1.0f)
            }
            val progressAnimatable = remember { Animatable(0f) }

            // Re-trigger animation from 0 to target value whenever screen is opened or appsCount updates
            LaunchedEffect(appsCount) {
                progressAnimatable.snapTo(0f)
                progressAnimatable.animateTo(
                    targetValue = targetFraction,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }

            val animatedRatio = if (targetFraction > 0f) (progressAnimatable.value / targetFraction).coerceIn(0f, 1f) else 0f
            val displayAppsCount = (appsCount * animatedRatio).toInt()

            val gaugeTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            // Circular Gauge Card for Stop
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
                        color = stopOrange,
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
                        text = displayAppsCount.toString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(id = R.string.stop_gauge_apps),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.stop_gauge_background),
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
                        text = stringResource(id = R.string.stop_gauge_zero),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 16.dp, y = (-24).dp)
                    )
                    Text(
                        text = stringResource(id = R.string.stop_gauge_active_count, appsCount),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-16).dp, y = (-24).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StopStatItem(
                    color = stopOrange,
                    title = stringResource(id = R.string.stop_stat_running_title),
                    value = stringResource(id = R.string.stop_stat_running_value, appsCount),
                    modifier = Modifier.weight(1f)
                )
                StopStatItem(
                    color = Color(0xFFFFB74D),
                    title = stringResource(id = R.string.stop_stat_ram_used),
                    value = ramInfo.usedString,
                    modifier = Modifier.weight(1f)
                )
                StopStatItem(
                    color = stopOrange,
                    title = stringResource(id = R.string.stop_stat_free_ram),
                    value = ramInfo.freeString,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Row (Memory Info, Whitelist, Running Apps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StopActionCircleButton(
                    icon = Icons.Default.Memory,
                    iconSize = 23.dp,
                    label = stringResource(id = R.string.stop_action_memory_info),
                    onClick = { showMemoryInfoDialog = true },
                    modifier = Modifier.weight(1f)
                )
                StopActionCircleButton(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    label = stringResource(id = R.string.stop_action_whitelist),
                    onClick = onOpenWhitelist,
                    modifier = Modifier.weight(1f)
                )
                StopActionCircleButton(
                    icon = Icons.Default.Android,
                    label = stringResource(id = R.string.stop_action_running_apps),
                    onClick = { showRunningAppsDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Memory Info Dialog
    if (showMemoryInfoDialog) {
        val usedPercentage = if (ramInfo.totalBytes > 0) ((ramInfo.usedBytes * 100) / ramInfo.totalBytes).toInt() else 0
        AlertDialog(
            onDismissRequest = { showMemoryInfoDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.stop_dialog_memory_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.stop_dialog_total_ram, ramInfo.totalString),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(id = R.string.stop_dialog_used_ram, ramInfo.usedString, usedPercentage),
                        color = Color(0xFFFFB74D),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.stop_dialog_free_ram, ramInfo.freeString),
                        color = stopOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = { showMemoryInfoDialog = false }) {
                    Text(text = stringResource(id = R.string.stop_dialog_close), color = stopOrange, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Running Apps Dialog
    if (showRunningAppsDialog) {
        AlertDialog(
            onDismissRequest = { showRunningAppsDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.stop_dialog_running_apps_title, installedApps.size),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (installedApps.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.stop_dialog_no_running_apps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(installedApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (app.icon != null) {
                                        val bitmap = remember(app.packageName) {
                                            try {
                                                app.icon.toBitmap().asImageBitmap()
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = app.name,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Android,
                                                contentDescription = app.name,
                                                tint = stopOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Android,
                                            contentDescription = app.name,
                                            tint = stopOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = app.name,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = { showRunningAppsDialog = false }) {
                    Text(text = stringResource(id = R.string.stop_dialog_close), color = stopOrange, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun StopStatItem(
    color: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(20.dp))
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
fun StopActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp
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
                modifier = Modifier.size(iconSize)
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
