package com.tkno.apex.ui.main

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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.tkno.apex.R
import com.tkno.apex.ui.component.BackButton
import com.tkno.apex.model.AppCacheInfo

enum class WhitelistType {
    STOP, CLEAN
}

@Composable
fun WhitelistScreen(
    type: WhitelistType,
    whitelistedPackages: Set<String>,
    allApps: List<AppCacheInfo>,
    onAddPackages: (Set<String>) -> Unit,
    onRemovePackage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val darkBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val accentBlue = Color(0xFF48AFFF)

    BackHandler(onBack = onBackClick)

    var showAddAppDialog by remember { mutableStateOf(false) }
    var selectedPackages by remember { mutableStateOf(setOf<String>()) }
    var isDialogSearchOpen by remember { mutableStateOf(false) }
    var dialogSearchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = showAddAppDialog) {
        showAddAppDialog = false
        selectedPackages = emptySet()
        isDialogSearchOpen = false
        dialogSearchQuery = ""
    }

    val whitelistedApps = remember(allApps, whitelistedPackages) {
        allApps.filter { it.packageName in whitelistedPackages }
    }

    val availableAppsToAdd = remember(allApps, whitelistedPackages) {
        allApps.filter { it.packageName !in whitelistedPackages }.sortedBy { it.name.lowercase() }
    }

    val filteredAvailableAppsToAdd = remember(availableAppsToAdd, dialogSearchQuery) {
        if (dialogSearchQuery.isBlank()) {
            availableAppsToAdd
        } else {
            val query = dialogSearchQuery.trim().lowercase()
            availableAppsToAdd.filter {
                it.name.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Top Bar Header with Back Arrow and Title
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
                    text = stringResource(R.string.whitelist_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main Content Area
            if (whitelistedPackages.isEmpty()) {
                // Empty State matching user's screenshot
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = stringResource(R.string.cd_whitelist_icon),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (type == WhitelistType.STOP)
                                stringResource(R.string.whitelist_empty_avoid_stop)
                            else
                                stringResource(R.string.whitelist_empty_avoid_clean),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            } else {
                // List of Whitelisted Apps
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(whitelistedApps, key = { it.packageName }) { app ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val pm = context.packageManager
                                val iconDrawable = remember(app.packageName) {
                                    try {
                                        pm.getApplicationIcon(app.packageName)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                if (iconDrawable != null) {
                                    Image(
                                        bitmap = iconDrawable.toBitmap().asImageBitmap(),
                                        contentDescription = app.name,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Android,
                                            contentDescription = null,
                                            tint = accentBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.name,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }

                                IconButton(onClick = { onRemovePackage(app.packageName) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.cd_remove),
                                        tint = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button (FAB) for adding apps to Whitelist
        FloatingActionButton(
            onClick = {
                selectedPackages = emptySet()
                isDialogSearchOpen = false
                dialogSearchQuery = ""
                showAddAppDialog = true
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_add_app),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        // Dialog for Multi-Selection of apps to add to Whitelist
        if (showAddAppDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddAppDialog = false
                    selectedPackages = emptySet()
                    isDialogSearchOpen = false
                    dialogSearchQuery = ""
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedPackages.isEmpty()) stringResource(R.string.whitelist_dialog_title_add) else stringResource(R.string.whitelist_dialog_title_selected, selectedPackages.size),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                isDialogSearchOpen = !isDialogSearchOpen
                                if (!isDialogSearchOpen) {
                                    dialogSearchQuery = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_apps),
                                tint = if (isDialogSearchOpen) accentBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isDialogSearchOpen) {
                            OutlinedTextField(
                                value = dialogSearchQuery,
                                onValueChange = { dialogSearchQuery = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.search_apps),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (dialogSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { dialogSearchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentBlue,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }

                        if (availableAppsToAdd.isEmpty()) {
                            Text(
                                text = stringResource(R.string.whitelist_dialog_all_apps_added),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        } else if (filteredAvailableAppsToAdd.isEmpty()) {
                            Text(
                                text = stringResource(R.string.apps_no_apps_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 350.dp)
                            ) {
                                items(filteredAvailableAppsToAdd, key = { it.packageName }) { app ->
                                val isSelected = app.packageName in selectedPackages
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent)
                                        .clickable {
                                            selectedPackages = if (isSelected) {
                                                selectedPackages - app.packageName
                                            } else {
                                                selectedPackages + app.packageName
                                            }
                                        }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val pm = context.packageManager
                                    val iconDrawable = remember(app.packageName) {
                                        try {
                                            pm.getApplicationIcon(app.packageName)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }

                                    if (iconDrawable != null) {
                                        Image(
                                            bitmap = iconDrawable.toBitmap().asImageBitmap(),
                                            contentDescription = app.name,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Android,
                                            contentDescription = null,
                                            tint = accentBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
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

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedPackages = if (checked) {
                                                selectedPackages + app.packageName
                                            } else {
                                                selectedPackages - app.packageName
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = accentBlue,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = cardBg,
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddAppDialog = false
                            selectedPackages = emptySet()
                            isDialogSearchOpen = false
                            dialogSearchQuery = ""
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedPackages.isNotEmpty()) {
                                onAddPackages(selectedPackages)
                                showAddAppDialog = false
                                selectedPackages = emptySet()
                                isDialogSearchOpen = false
                                dialogSearchQuery = ""
                            }
                        },
                        enabled = selectedPackages.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentBlue,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (selectedPackages.isEmpty()) stringResource(R.string.whitelist_dialog_add_btn) else stringResource(R.string.whitelist_dialog_add_count_btn, selectedPackages.size),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}

