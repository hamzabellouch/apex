package com.tkno.apex.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.apex.R
import com.tkno.apex.ui.icon.LeftPanelOpen

@Composable
fun StatisticsScreen(
    onOpenDrawer: () -> Unit = {}
) {
    val darkBg = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Top Bar Header with Menu Icon & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = LeftPanelOpen,
                            contentDescription = stringResource(id = R.string.nav_menu),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.nav_statistics),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Centered Message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "This is currently unavailable.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
