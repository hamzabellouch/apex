package com.tkno.apex.ui.main

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tkno.apex.R
import com.tkno.apex.ui.component.BackButton
import com.tkno.apex.model.AppCacheInfo
import com.tkno.apex.util.AppStorageHelper

@Composable
fun AnalyzeScreen(
    installedApps: List<AppCacheInfo>,
    totalCacheBytes: Long,
    currentCleaningPackage: String?,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkBg = MaterialTheme.colorScheme.background
    val accentBlue = Color(0xFF48AFFF)
    val redProgressColor = Color(0xFFEC4899)

    val totalCacheString = AppStorageHelper.formatSize(totalCacheBytes)

    val listState = rememberLazyListState()

    // Auto-scroll list to follow the app currently being cleaned
    LaunchedEffect(currentCleaningPackage) {
        currentCleaningPackage?.let { pkg ->
            val index = installedApps.indexOfFirst { it.packageName == pkg }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
    ) {
        // Top Bar
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
                text = stringResource(R.string.analyze_screen_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Cache Stats Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.analyze_cache_apps_count, installedApps.size),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = totalCacheString,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Red progress indicator
        LinearProgressIndicator(
            progress = { 1.0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = redProgressColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App list to clean
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (installedApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.analyze_no_cache_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installedApps) { app ->
                        AppCacheItem(app = app)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    }
                }
            }

            // CLEAR Button (Bottom Right Floating)
            if (installedApps.isNotEmpty()) {
                Button(
                    onClick = onClearClick,
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = stringResource(R.string.cd_broom_icon),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.analyze_clear_button),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

