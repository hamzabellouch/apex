package com.tkno.apex.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ToggleOff
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
import com.tkno.apex.R

@Composable
fun AccessibilityPermissionScreen(
    onContinueClick: () -> Unit,
    onCancelClick: () -> Unit,
    descriptionResId: Int = R.string.accessibility_permission_desc1,
    modifier: Modifier = Modifier
) {
    val darkBg = MaterialTheme.colorScheme.background
    val isStopMode = descriptionResId == R.string.accessibility_permission_desc1_stop
    val accentBlue = Color(0xFF48AFFF)
    val stopOrange = Color(0xFFFF6D00)
    val activeColor = if (isStopMode) stopOrange else accentBlue
    val circleContainerBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val cancelBtnBg = MaterialTheme.colorScheme.surfaceContainer
    val bodyTextColor = MaterialTheme.colorScheme.onSurfaceVariant

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
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ToggleOff,
                    contentDescription = stringResource(R.string.cd_toggle_icon),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.permission_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center Icon Container (Dark Circle with Human Figure)
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .background(circleContainerBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessibilityNew,
                    contentDescription = stringResource(R.string.cd_accessibility_icon),
                    tint = activeColor,
                    modifier = Modifier.size(51.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Screen Title
            Text(
                text = stringResource(R.string.accessibility_permission_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Paragraph 1
            Text(
                text = stringResource(descriptionResId),
                color = bodyTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Paragraph 2
            Text(
                text = stringResource(R.string.accessibility_permission_desc2),
                color = bodyTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Buttons Row (CANCEL & CONTINUE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CANCEL Button
                Button(
                    onClick = onCancelClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cancelBtnBg),
                    modifier = Modifier
                        .weight(1f)
                        .height(39.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel_uppercase),
                        color = activeColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // CONTINUE Button
                Button(
                    onClick = onContinueClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(39.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_continue_uppercase),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

