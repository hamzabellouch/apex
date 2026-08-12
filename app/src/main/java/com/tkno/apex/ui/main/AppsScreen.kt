package com.tkno.apex.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tkno.apex.R
import com.tkno.apex.model.AppCacheInfo
import com.tkno.apex.ui.component.rememberThumbContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppStatusFilter(val labelRes: Int) {
    ALL(R.string.filter_by_all),
    ENABLED(R.string.filter_by_enabled),
    DISABLED(R.string.filter_by_disabled)
}

enum class AppSortOption(val labelRes: Int) {
    NAME(R.string.sort_by_name),
    SIZE(R.string.sort_by_size),
    LAST_USED(R.string.sort_by_last_used),
    LAST_UPDATED(R.string.sort_by_last_updated)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    installedApps: List<AppCacheInfo>,
    currentCleaningPackage: String?,
    modifier: Modifier = Modifier
) {
    val darkBg = MaterialTheme.colorScheme.background
    val accentBlue = Color(0xFF48AFFF)
    val androidGreen = Color(0xFF3DDC84)

    var showSystemApps by remember { mutableStateOf(false) }
    var currentStatusFilter by remember { mutableStateOf(AppStatusFilter.ALL) }
    var currentSortOption by remember { mutableStateOf(AppSortOption.NAME) }

    var showFilterSortBottomSheet by remember { mutableStateOf(false) }

    var selectedAppForDetail by remember { mutableStateOf<AppCacheInfo?>(null) }

    BackHandler(enabled = selectedAppForDetail != null) {
        selectedAppForDetail = null
    }

    val listState = rememberLazyListState()

    // Filter and Sort apps
    val filteredApps = remember(installedApps, showSystemApps, currentStatusFilter, currentSortOption) {
        var list = installedApps

        // 1. Show system apps toggle
        if (!showSystemApps) {
            list = list.filter { !it.isSystemApp }
        }

        // 2. Filter by All / Enabled / Disabled
        list = when (currentStatusFilter) {
            AppStatusFilter.ALL -> list
            AppStatusFilter.ENABLED -> list.filter { it.isEnabled }
            AppStatusFilter.DISABLED -> list.filter { !it.isEnabled }
        }

        // 3. Sort by Name / Size / Last used / Last updated
        when (currentSortOption) {
            AppSortOption.NAME -> list.sortedBy { it.name.lowercase() }
            AppSortOption.SIZE -> list.sortedByDescending { it.appSizeBytes }
            AppSortOption.LAST_USED -> list.sortedByDescending { if (it.lastUsedTime > 0) it.lastUsedTime else it.installTime }
            AppSortOption.LAST_UPDATED -> list.sortedByDescending { if (it.lastUpdateTime > 0) it.lastUpdateTime else it.installTime }
        }
    }

    // Auto-scroll list to follow the app currently being cleaned
    LaunchedEffect(currentCleaningPackage) {
        currentCleaningPackage?.let { pkg ->
            val index = filteredApps.indexOfFirst { it.packageName == pkg }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    AnimatedContent(
        targetState = selectedAppForDetail,
        transitionSpec = {
            if (targetState != null) {
                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))) togetherWith
                        fadeOut(animationSpec = tween(180))
            } else {
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(180))
            }
        },
        label = "AppDetailTransition"
    ) { appForDetail ->
        if (appForDetail != null) {
            val detailApp = installedApps.find { it.packageName == appForDetail.packageName } ?: appForDetail
            AppDetailScreen(
                app = detailApp,
                onBackClick = { selectedAppForDetail = null },
                modifier = modifier
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(darkBg)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            ) {
                // Top Bar Header with Title on Left, 3-dots Menu Icon on Far Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = stringResource(R.string.apps_screen_title),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.apps_listed_count, filteredApps.size),
                            color = androidGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    // 3-dots Menu Button
                    IconButton(onClick = { showFilterSortBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.filter_and_sort),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.apps_no_apps_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Apps List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                AppCacheItem(
                                    app = app,
                                    onClick = { selectedAppForDetail = app }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSortBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilterSortBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = CircleShape
                ) {
                    Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
                }
            },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                // Sheet Title
                Text(
                    text = stringResource(R.string.filter_and_sort),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 1. Show system apps (Row with Switch)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.show_system_apps),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = showSystemApps,
                        onCheckedChange = { showSystemApps = it },
                        thumbContent = rememberThumbContent(isChecked = showSystemApps)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Filter by Section Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.filter_by),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }

                // Filter Radio Options (All, Enabled, Disabled)
                AppStatusFilter.values().forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { currentStatusFilter = filter }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentStatusFilter == filter),
                            onClick = { currentStatusFilter = filter },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentBlue,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(filter.labelRes),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = if (currentStatusFilter == filter) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Sort by Section Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sort_by),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }

                // Sort Radio Options (Name, Size, Last used, Last updated)
                AppSortOption.values().forEach { sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { currentSortOption = sort }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentSortOption == sort),
                            onClick = { currentSortOption = sort },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentBlue,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(sort.labelRes),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = if (currentSortOption == sort) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun AppCacheItem(
    app: AppCacheInfo,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(app.installTime) {
        if (app.installTime > 0) dateFormat.format(Date(app.installTime)) else ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick?.invoke() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon Box
        Box(
            modifier = Modifier
                .size(48.dp)
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
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = app.name,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = app.name,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // App details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_size_prefix, app.appSizeString),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                if (formattedDate.isNotEmpty()) {
                    Text(
                        text = "• $formattedDate",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Chevron Right
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_go_to_details),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}
