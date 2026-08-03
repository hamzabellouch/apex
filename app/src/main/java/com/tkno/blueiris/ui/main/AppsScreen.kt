package com.tkno.blueiris.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
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
import com.tkno.blueiris.R
import com.tkno.blueiris.model.AppCacheInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppFilterType(val labelRes: Int) {
    ALL(R.string.app_filter_all),
    USER(R.string.app_filter_user),
    SYSTEM(R.string.app_filter_system)
}

enum class AppSortType(val labelRes: Int) {
    NAME(R.string.app_sort_by_name),
    DATE(R.string.app_sort_by_date),
    SIZE(R.string.app_sort_by_size)
}

@Composable
fun AppsScreen(
    installedApps: List<AppCacheInfo>,
    currentCleaningPackage: String?,
    onCleanSelectedApps: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkBg = MaterialTheme.colorScheme.background
    val accentBlue = Color(0xFF48AFFF)

    var currentFilter by remember { mutableStateOf(AppFilterType.ALL) }
    var currentSort by remember { mutableStateOf(AppSortType.NAME) }

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedPackages by remember { mutableStateOf(emptySet<String>()) }
    val isSelectionMode = selectedPackages.isNotEmpty()

    var selectedAppForDetail by remember { mutableStateOf<AppCacheInfo?>(null) }

    BackHandler(enabled = selectedAppForDetail != null) {
        selectedAppForDetail = null
    }

    val listState = rememberLazyListState()

    // Filter and Sort apps
    val filteredApps = remember(installedApps, currentFilter, currentSort) {
        val list = when (currentFilter) {
            AppFilterType.ALL -> installedApps
            AppFilterType.USER -> installedApps.filter { !it.isSystemApp }
            AppFilterType.SYSTEM -> installedApps.filter { it.isSystemApp }
        }
        when (currentSort) {
            AppSortType.NAME -> list.sortedBy { it.name.lowercase() }
            AppSortType.DATE -> list.sortedByDescending { it.installTime }
            AppSortType.SIZE -> list.sortedByDescending { it.appSizeBytes }
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
        // Top Bar Header with Title on Left, Filter and Sort Menus on Far Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.apps_screen_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.apps_listed_count, filteredApps.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Right Action Buttons for Filter & Sort
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Filter Dropdown Button
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.cd_filter_apps),
                            tint = if (currentFilter != AppFilterType.ALL) accentBlue else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        AppFilterType.values().forEach { filter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(filter.labelRes),
                                        color = if (currentFilter == filter) accentBlue else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (currentFilter == filter) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    currentFilter = filter
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }

                // 2. Sort Dropdown Button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.cd_sort_apps),
                            tint = if (currentSort != AppSortType.NAME) accentBlue else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        AppSortType.values().forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(sort.labelRes),
                                        color = if (currentSort == sort) accentBlue else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (currentSort == sort) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    currentSort = sort
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
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
                    contentPadding = PaddingValues(bottom = if (isSelectionMode) 72.dp else 16.dp)
                ) {
                    items(filteredApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        
                        AppCacheItem(
                            app = app,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onLongClick = {
                                selectedPackages = if (isSelected) {
                                    selectedPackages - app.packageName
                                } else {
                                    selectedPackages + app.packageName
                                }
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    selectedPackages = if (isSelected) {
                                        selectedPackages - app.packageName
                                    } else {
                                        selectedPackages + app.packageName
                                    }
                                } else {
                                    selectedAppForDetail = app
                                }
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    }
                }
            }

            // Bottom Selection Action Bar
            if (isSelectionMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedPackages = emptySet() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_clear_selection),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.apps_selected_count, selectedPackages.size),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            onCleanSelectedApps(selectedPackages.toList())
                            selectedPackages = emptySet()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = stringResource(R.string.cd_clean_selected_apps),
                            tint = accentBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCacheItem(
    app: AppCacheInfo,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accentBlue = Color(0xFF48AFFF)
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(app.installTime) {
        if (app.installTime > 0) dateFormat.format(Date(app.installTime)) else ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onLongClick = { onLongClick?.invoke() },
                onClick = { onClick?.invoke() }
            )
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

            // Selection overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000), CircleShape)
                        .background(accentBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.cd_selected),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
            imageVector = Icons.Default.ChevronRight,
            contentDescription = stringResource(R.string.cd_go_to_details),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}
