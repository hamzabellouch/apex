package com.tkno.apex.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tkno.apex.R
import com.tkno.apex.ui.component.BackButton
import com.tkno.apex.model.AppCacheInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppDetailScreen(
    app: AppCacheInfo,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val darkBg = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val androidGreen = Color(0xFF3DDC84)

    BackHandler(onBack = onBackClick)

    // Fetch package details
    val (versionName, lastUpdateTime, permissionCount) = remember(app.packageName) {
        try {
            val pm = context.packageManager
            val pInfo = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
            val ver = pInfo.versionName ?: "1.0"
            val lastUp = pInfo.lastUpdateTime
            val permCount = pInfo.requestedPermissions?.size ?: 0
            Triple(ver, lastUp, permCount)
        } catch (e: Exception) {
            Triple("1.0", 0L, 0)
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    val formattedInstallDate = remember(app.installTime) {
        if (app.installTime > 0) dateFormat.format(Date(app.installTime)) else "N/A"
    }

    val formattedUpdateDate = remember(lastUpdateTime, app.installTime) {
        val timeToFormat = if (lastUpdateTime > 0) lastUpdateTime else app.installTime
        if (timeToFormat > 0) dateFormat.format(Date(timeToFormat)) else formattedInstallDate
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // Scrollable Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header: Back Button directly on background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onBackClick)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // App Icon (Compact size: 72.dp)
            Box(
                modifier = Modifier
                    .size(72.dp)
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
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = app.name,
                            tint = textPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = app.name,
                        tint = textPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Title (White in Dark mode, Black in Light mode)
            Text(
                text = app.name,
                color = textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Version String (White in Dark mode, Black in Light mode)
            Text(
                text = stringResource(R.string.app_detail_version, versionName),
                color = textPrimary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata Rows Table (Compact spacing)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                AppDetailMetadataRow(
                    label = stringResource(R.string.app_detail_apk_size),
                    value = app.appSizeString,
                    textColor = textPrimary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                AppDetailMetadataRow(
                    label = stringResource(R.string.app_detail_install_date),
                    value = formattedInstallDate,
                    textColor = textPrimary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                AppDetailMetadataRow(
                    label = stringResource(R.string.app_detail_last_update),
                    value = formattedUpdateDate,
                    textColor = textPrimary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                AppDetailMetadataRow(
                    label = stringResource(R.string.app_detail_requested_permissions),
                    value = permissionCount.toString(),
                    textColor = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Row (Uninstall, Update, Launch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Uninstall Button
                AppDetailActionButton(
                    icon = Icons.Outlined.Delete,
                    label = stringResource(R.string.app_detail_uninstall),
                    containerColor = Color(0xFFE53935),
                    textColor = textPrimary,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:${app.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Update Button
                AppDetailActionButton(
                    icon = Icons.Outlined.SystemUpdate,
                    label = stringResource(R.string.app_detail_update),
                    containerColor = Color(0xFF2E7D32),
                    textColor = textPrimary,
                    onClick = {
                        try {
                            val pm = context.packageManager
                            val installer = try {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                    pm.getInstallSourceInfo(app.packageName).installingPackageName
                                } else {
                                    @Suppress("DEPRECATION")
                                    pm.getInstallerPackageName(app.packageName)
                                }
                            } catch (e: Exception) {
                                null
                            }

                            val intent = if (installer == "com.sec.android.app.samsungapps") {
                                Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/${app.packageName}"))
                            } else {
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}"))
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to web browser Play Store link
                            try {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")
                                )
                                context.startActivity(webIntent)
                            } catch (ex: Exception) {
                                Toast.makeText(context, context.getString(R.string.toast_cannot_open_store), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                // Launch Button
                AppDetailActionButton(
                    icon = Icons.AutoMirrored.Outlined.Launch,
                    label = stringResource(R.string.app_detail_launch),
                    containerColor = Color(0xFF8E24AA),
                    textColor = textPrimary,
                    onClick = {
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_cannot_launch_app), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.toast_cannot_launch_app), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Prominent Green APP INFO Button - Exact same size/dimensions as Done button (height 40.dp, RoundedCornerShape 20.dp)
            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${app.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.app_detail_app_info),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AppDetailMetadataRow(
    label: String,
    value: String,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = textColor,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AppDetailActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(containerColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = containerColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp
        )
    }
}

