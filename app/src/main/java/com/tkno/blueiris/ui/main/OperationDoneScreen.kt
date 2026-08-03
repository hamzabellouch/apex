package com.tkno.blueiris.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.blueiris.R
import com.tkno.blueiris.util.AppStorageHelper

@Composable
fun OperationDoneScreen(
    actionType: String, // "STOP" or "CLEAN"
    completedAppsCount: Int,
    cleanedCacheBytes: Long = 0L,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkBg = MaterialTheme.colorScheme.background
    val accentBlue = Color(0xFF48AFFF)
    val circleBlue = Color(0xFF64B5F6)

    val countText = if (completedAppsCount == 1) stringResource(R.string.done_count_apps_singular) else stringResource(R.string.done_count_apps_plural, completedAppsCount)
    val cleanedText = AppStorageHelper.formatSize(cleanedCacheBytes)
    val subtitle = if (actionType == "STOP") stringResource(R.string.done_stopped_subtitle, countText) else stringResource(R.string.done_cleaned_subtitle, cleanedText)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Center Solid Blue Circle Container with White Checkmark
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .background(circleBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_check_icon),
                    tint = Color.White,
                    modifier = Modifier.size(51.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title "DONE"
            Text(
                text = stringResource(R.string.done_button),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle e.g. "Stopped: 3 Apps" or "Cleaned: 500 MB"
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Full Width DONE Button
            Button(
                onClick = onDoneClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.done_button),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

