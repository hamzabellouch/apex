package com.tkno.apex.ui.main

import com.tkno.apex.ui.icon.LeftPanelClose

import android.app.LocaleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.apex.R
import com.tkno.apex.ui.component.*
import com.tkno.apex.ui.page.AppUpdater
import com.tkno.apex.ui.page.settings.BasePreferencePage
import com.tkno.apex.ui.page.settings.about.UpdatePage
import com.tkno.apex.ui.svg.drawablevectors.DynamicColorImageVectors
import com.tkno.apex.ui.svg.drawablevectors.coder
import java.util.Locale

enum class MenuSubScreen {
    Main, Settings, GeneralSettings, CustomSpeedSettings, LookAndFeel, Languages, DarkTheme, Sponsor, Troubleshooting, About, Credits, Update
}

@Composable
fun MenuScreen(
    onSubScreenStateChanged: (Boolean) -> Unit = {}
) {
    var currentSubScreen by remember { mutableStateOf(MenuSubScreen.Main) }

    LaunchedEffect(currentSubScreen) {
        onSubScreenStateChanged(currentSubScreen != MenuSubScreen.Main)
    }

    BackHandler(enabled = currentSubScreen != MenuSubScreen.Main) {
        when (currentSubScreen) {
            MenuSubScreen.GeneralSettings -> currentSubScreen = MenuSubScreen.Settings
            MenuSubScreen.CustomSpeedSettings -> currentSubScreen = MenuSubScreen.GeneralSettings
            MenuSubScreen.Languages -> currentSubScreen = MenuSubScreen.LookAndFeel
            MenuSubScreen.DarkTheme -> currentSubScreen = MenuSubScreen.LookAndFeel
            MenuSubScreen.LookAndFeel -> currentSubScreen = MenuSubScreen.Settings
            MenuSubScreen.Credits -> currentSubScreen = MenuSubScreen.About
            MenuSubScreen.Update -> currentSubScreen = MenuSubScreen.About
            else -> currentSubScreen = MenuSubScreen.Main
        }
    }

    AnimatedContent(
        targetState = currentSubScreen,
        transitionSpec = {
            if (targetState != MenuSubScreen.Main) {
                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))) togetherWith
                        fadeOut(animationSpec = tween(180))
            } else {
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(180))
            }
        },
        label = "MenuSubScreenTransition"
    ) { subScreen ->
        when (subScreen) {
            MenuSubScreen.Main -> MainMenuList(onNavigateTo = { currentSubScreen = it })
            MenuSubScreen.Settings -> SettingsPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.Main },
                onNavigateTo = { route ->
                    when (route) {
                        "general" -> currentSubScreen = MenuSubScreen.GeneralSettings
                        "appearance" -> currentSubScreen = MenuSubScreen.LookAndFeel
                    }
                }
            )
            MenuSubScreen.GeneralSettings -> GeneralSettingsPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.Settings },
                onNavigateToCustomSpeed = { currentSubScreen = MenuSubScreen.CustomSpeedSettings }
            )
            MenuSubScreen.CustomSpeedSettings -> CustomSpeedSettingsPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.GeneralSettings }
            )
            MenuSubScreen.LookAndFeel -> AppearancePreferences(
                onNavigateBack = { currentSubScreen = MenuSubScreen.Settings },
                onNavigateTo = { route ->
                    if (route == "languages") currentSubScreen = MenuSubScreen.Languages
                    else if (route == "dark_theme") currentSubScreen = MenuSubScreen.DarkTheme
                }
            )
            MenuSubScreen.DarkTheme -> DarkThemePreferences(
                onNavigateBack = { currentSubScreen = MenuSubScreen.LookAndFeel }
            )
            MenuSubScreen.Languages -> LanguagesPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.LookAndFeel }
            )
            MenuSubScreen.Sponsor -> SponsorsPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.Main }
            )
            MenuSubScreen.Troubleshooting -> TroubleShootingPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.Main }
            )
            MenuSubScreen.About -> AboutPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.Main },
                onNavigateToCreditsPage = { currentSubScreen = MenuSubScreen.Credits },
                onNavigateToUpdatePage = { currentSubScreen = MenuSubScreen.Update }
            )
            MenuSubScreen.Credits -> CreditsPage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.About }
            )
            MenuSubScreen.Update -> UpdatePage(
                onNavigateBack = { currentSubScreen = MenuSubScreen.About },
                triggerUpdate = false
            )
        }
    }
}

@Composable
fun MainMenuList(
    currentSubScreen: SubScreen = SubScreen.None,
    onCloseDrawer: () -> Unit = {},
    onNavigateTo: (MenuSubScreen) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    IconButton(onClick = onCloseDrawer) {
                        Icon(
                            imageVector = LeftPanelClose,
                            contentDescription = stringResource(id = R.string.nav_menu),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.menu),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.menu_options_listed_count, 4),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            val isSettingsSelected = currentSubScreen == SubScreen.Settings ||
                currentSubScreen == SubScreen.GeneralSettings ||
                currentSubScreen == SubScreen.CustomSpeedSettings ||
                currentSubScreen == SubScreen.LookAndFeel ||
                currentSubScreen == SubScreen.Languages ||
                currentSubScreen == SubScreen.DarkTheme

            val isSponsorSelected = currentSubScreen == SubScreen.Sponsor

            val isTroubleshootingSelected = currentSubScreen == SubScreen.Troubleshooting

            val isAboutSelected = currentSubScreen == SubScreen.About ||
                currentSubScreen == SubScreen.Credits ||
                currentSubScreen == SubScreen.Update

            val drawerItemColors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                selectedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface
            )
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.settings)) },
                    icon = { Icon(if (isSettingsSelected) Icons.Default.Settings else Icons.Outlined.Settings, null) },
                    onClick = { onNavigateTo(MenuSubScreen.Settings) },
                    selected = isSettingsSelected,
                    colors = drawerItemColors,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.sponsor)) },
                    icon = { Icon(if (isSponsorSelected) Icons.Default.VolunteerActivism else Icons.Outlined.VolunteerActivism, null) },
                    onClick = { onNavigateTo(MenuSubScreen.Sponsor) },
                    selected = isSponsorSelected,
                    colors = drawerItemColors,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.trouble_shooting)) },
                    icon = { Icon(if (isTroubleshootingSelected) Icons.Default.BugReport else Icons.Outlined.BugReport, null) },
                    onClick = { onNavigateTo(MenuSubScreen.Troubleshooting) },
                    selected = isTroubleshootingSelected,
                    colors = drawerItemColors,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.about)) },
                    icon = { Icon(if (isAboutSelected) Icons.Default.Info else Icons.Outlined.Info, null) },
                    onClick = { onNavigateTo(MenuSubScreen.About) },
                    selected = isAboutSelected,
                    colors = drawerItemColors,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)
                )
            }
        }
    }
}

/* ---------------- SettingsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    BasePreferencePage(
        title = stringResource(id = R.string.settings),
        onBack = onNavigateBack,
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
            item {
                SettingItem(
                    title = stringResource(id = R.string.general_settings),
                    description = stringResource(id = R.string.general_settings_desc),
                    icon = Icons.Rounded.SettingsApplications,
                ) {
                    onNavigateTo("general")
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.look_and_feel),
                    description = stringResource(id = R.string.display_settings),
                    icon = Icons.Rounded.Palette,
                ) {
                    onNavigateTo("appearance")
                }
            }
        }
    }
}

/* ---------------- GeneralSettingsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsPage(
    onNavigateBack: () -> Unit,
    onNavigateToCustomSpeed: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }
    var isBoostMode by remember { mutableStateOf(prefs.getBoolean("turbo_mode", prefs.getBoolean("boost_mode", false))) }
    var isCustomSpeedEnabled by remember { mutableStateOf(prefs.getBoolean("custom_speed_enabled", false)) }

    var forceStopMode by remember { mutableIntStateOf(prefs.getInt("force_stop_mode", 1)) }
    var showForceStopModeDialog by remember { mutableStateOf(false) }

    var cleaningMode by remember { mutableIntStateOf(prefs.getInt("cleaning_mode", 1)) }
    var isIgnoreTinyCache by remember { mutableStateOf(prefs.getBoolean("ignore_tiny_cache", false)) }
    var isGestureFallbackEnabled by remember { mutableStateOf(prefs.getBoolean("gesture_fallback_enabled", false)) }
    var showCleaningModeDialog by remember { mutableStateOf(false) }

    BasePreferencePage(
        title = stringResource(id = R.string.general_settings),
        onBack = onNavigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PreferenceGroupTitle(title = stringResource(id = R.string.performance_and_speed_title))

            PreferenceSwitch(
                title = stringResource(id = R.string.boost_mode),
                description = stringResource(id = R.string.boost_mode_desc),
                icon = Icons.Outlined.Speed,
                isChecked = isBoostMode,
                onClick = {
                    val newValue = !isBoostMode
                    isBoostMode = newValue
                    prefs.edit().putBoolean("turbo_mode", newValue).putBoolean("boost_mode", newValue).apply()
                }
            )

            PreferenceSwitchWithDivider(
                title = stringResource(id = R.string.custom_speed_mode),
                description = stringResource(id = R.string.custom_speed_mode_desc),
                icon = Icons.Outlined.Tune,
                isChecked = isCustomSpeedEnabled,
                onChecked = {
                    val newValue = !isCustomSpeedEnabled
                    isCustomSpeedEnabled = newValue
                    prefs.edit().putBoolean("custom_speed_enabled", newValue).apply()
                },
                onClick = onNavigateToCustomSpeed
            )

            PreferenceGroupTitle(
                title = stringResource(id = R.string.stop_process_title),
                color = Color(0xFFE56B40)
            )

            val forceStopModeText = when (forceStopMode) {
                0 -> stringResource(id = R.string.cleaning_mode_manual)
                1 -> stringResource(id = R.string.cleaning_mode_automatic)
                else -> stringResource(id = R.string.cleaning_mode_ask_each_time)
            }

            PreferenceChevronItem(
                title = stringResource(id = R.string.force_stop_mode_title),
                description = forceStopModeText,
                icon = Icons.Default.Block,
                onClick = { showForceStopModeDialog = true }
            )

            PreferenceGroupTitle(title = stringResource(id = R.string.clean_process_title))

            val cleaningModeText = when (cleaningMode) {
                0 -> stringResource(id = R.string.cleaning_mode_manual)
                1 -> stringResource(id = R.string.cleaning_mode_automatic)
                else -> stringResource(id = R.string.cleaning_mode_ask_each_time)
            }

            PreferenceChevronItem(
                title = stringResource(id = R.string.cleaning_mode_title),
                description = cleaningModeText,
                icon = Icons.Outlined.CleaningServices,
                onClick = { showCleaningModeDialog = true }
            )

            PreferenceCheckbox(
                title = stringResource(id = R.string.ignore_tiny_cache_title),
                description = stringResource(id = R.string.ignore_tiny_cache_desc),
                icon = Icons.Outlined.Storage,
                isChecked = isIgnoreTinyCache,
                onClick = {
                    val newValue = !isIgnoreTinyCache
                    isIgnoreTinyCache = newValue
                    prefs.edit().putBoolean("ignore_tiny_cache", newValue).apply()
                }
            )

            PreferenceSwitch(
                title = stringResource(id = R.string.gesture_fallback_title),
                description = stringResource(id = R.string.gesture_fallback_desc),
                icon = Icons.Outlined.TouchApp,
                isChecked = isGestureFallbackEnabled,
                onClick = {
                    val newValue = !isGestureFallbackEnabled
                    isGestureFallbackEnabled = newValue
                    prefs.edit().putBoolean("gesture_fallback_enabled", newValue).apply()
                }
            )
        }
    }

    if (showForceStopModeDialog) {
        ForceStopModeDialog(
            currentMode = forceStopMode,
            onSelectMode = { newMode ->
                forceStopMode = newMode
                prefs.edit().putInt("force_stop_mode", newMode).apply()
                showForceStopModeDialog = false
            },
            onDismiss = { showForceStopModeDialog = false }
        )
    }

    if (showCleaningModeDialog) {
        CleaningModeDialog(
            currentMode = cleaningMode,
            onSelectMode = { newMode ->
                cleaningMode = newMode
                prefs.edit().putInt("cleaning_mode", newMode).apply()
                showCleaningModeDialog = false
            },
            onDismiss = { showCleaningModeDialog = false }
        )
    }
}

@Composable
fun ForceStopModeDialog(
    currentMode: Int,
    onSelectMode: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.force_stop_mode_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                val options = listOf(
                    0 to stringResource(id = R.string.cleaning_mode_manual),
                    1 to stringResource(id = R.string.cleaning_mode_automatic),
                    2 to stringResource(id = R.string.cleaning_mode_ask_each_time)
                )
                options.forEach { (modeValue, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(modeValue) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentMode == modeValue),
                            onClick = { onSelectMode(modeValue) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF48AFFF)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Color(0xFF48AFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun CleaningModeDialog(
    currentMode: Int,
    onSelectMode: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.cleaning_mode_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                val options = listOf(
                    0 to stringResource(id = R.string.cleaning_mode_manual),
                    1 to stringResource(id = R.string.cleaning_mode_automatic),
                    2 to stringResource(id = R.string.cleaning_mode_ask_each_time)
                )
                options.forEach { (modeValue, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(modeValue) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentMode == modeValue),
                            onClick = { onSelectMode(modeValue) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF48AFFF)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Color(0xFF48AFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun PreferenceGroupTitle(
    title: String,
    color: Color = Color(0xFF48AFFF)
) {
    Text(
        text = title,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

/* ---------------- CustomSpeedSettingsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSpeedSettingsPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }

    // Stop Mode state
    var stopScanDelay by remember { mutableLongStateOf(prefs.getLong("stop_scan_delay", 140L)) }
    var stopPageWait by remember { mutableLongStateOf(prefs.getLong("stop_page_wait", 450L)) }
    var stopConfirmWait by remember { mutableLongStateOf(prefs.getLong("stop_confirm_wait", 150L)) }
    var stopNextDelay by remember { mutableLongStateOf(prefs.getLong("stop_next_delay", 260L)) }

    // Clean Mode state
    var cleanScanDelay by remember { mutableLongStateOf(prefs.getLong("clean_scan_delay", 140L)) }
    var cleanPageWait by remember { mutableLongStateOf(prefs.getLong("clean_page_wait", 450L)) }
    var cleanStepPause by remember { mutableLongStateOf(prefs.getLong("clean_step_pause", 350L)) }
    var cleanNextDelay by remember { mutableLongStateOf(prefs.getLong("clean_next_delay", 260L)) }

    BasePreferencePage(
        title = stringResource(id = R.string.custom_speed_settings),
        onBack = onNavigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // STOP SECTION
            PreferenceGroupTitle(title = stringResource(id = R.string.stop_section_title))

            TimingSliderItem(
                title = stringResource(id = R.string.scan_delay_title),
                valueMs = stopScanDelay,
                rangeMs = 50f..300f,
                onValueChange = {
                    stopScanDelay = it
                    prefs.edit().putLong("stop_scan_delay", it).apply()
                }
            )

            TimingSliderItem(
                title = stringResource(id = R.string.page_wait_title),
                valueMs = stopPageWait,
                rangeMs = 200f..800f,
                onValueChange = {
                    stopPageWait = it
                    prefs.edit().putLong("stop_page_wait", it).apply()
                }
            )

            TimingSliderItem(
                title = stringResource(id = R.string.confirm_wait_title),
                valueMs = stopConfirmWait,
                rangeMs = 50f..500f,
                onValueChange = {
                    stopConfirmWait = it
                    prefs.edit().putLong("stop_confirm_wait", it).apply()
                }
            )

            TimingSliderItem(
                title = stringResource(id = R.string.next_delay_title),
                valueMs = stopNextDelay,
                rangeMs = 100f..600f,
                onValueChange = {
                    stopNextDelay = it
                    prefs.edit().putLong("stop_next_delay", it).apply()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CLEAN SECTION
            PreferenceGroupTitle(title = stringResource(id = R.string.clean_section_title))

            TimingSliderItem(
                title = stringResource(id = R.string.scan_delay_title),
                valueMs = cleanScanDelay,
                rangeMs = 50f..300f,
                onValueChange = {
                    cleanScanDelay = it
                    prefs.edit().putLong("clean_scan_delay", it).apply()
                }
            )

            TimingSliderItem(
                title = stringResource(id = R.string.page_wait_title),
                valueMs = cleanPageWait,
                rangeMs = 200f..800f,
                onValueChange = {
                    cleanPageWait = it
                    prefs.edit().putLong("clean_page_wait", it).apply()
                }
            )

            TimingSliderItem(
                title = stringResource(id = R.string.step_pause_title),
                valueMs = cleanStepPause,
                rangeMs = 100f..600f,
                onValueChange = {
                    cleanStepPause = it
                    prefs.edit().putLong("clean_step_pause", it).apply()
                }
            )

            TimingSliderItem(
                title = stringResource(id = R.string.next_delay_title),
                valueMs = cleanNextDelay,
                rangeMs = 100f..600f,
                onValueChange = {
                    cleanNextDelay = it
                    prefs.edit().putLong("clean_next_delay", it).apply()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RESTORE DEFAULTS BUTTON
            OutlinedButton(
                onClick = {
                    stopScanDelay = 140L
                    stopPageWait = 450L
                    stopConfirmWait = 150L
                    stopNextDelay = 260L
                    cleanScanDelay = 140L
                    cleanPageWait = 450L
                    cleanStepPause = 350L
                    cleanNextDelay = 260L

                    prefs.edit()
                        .putLong("stop_scan_delay", 140L)
                        .putLong("stop_page_wait", 450L)
                        .putLong("stop_confirm_wait", 150L)
                        .putLong("stop_next_delay", 260L)
                        .putLong("clean_scan_delay", 140L)
                        .putLong("clean_page_wait", 450L)
                        .putLong("clean_step_pause", 350L)
                        .putLong("clean_next_delay", 260L)
                        .apply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.restore_defaults))
            }
        }
    }
}

@Composable
fun TimingSliderItem(
    title: String,
    valueMs: Long,
    rangeMs: ClosedFloatingPointRange<Float>,
    unitText: String = "ms",
    onValueChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$valueMs $unitText",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF48AFFF)
            )
        }
        Slider(
            value = valueMs.toFloat(),
            onValueChange = { onValueChange(it.toLong()) },
            valueRange = rangeMs,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* ---------------- AppearancePreferences ---------------- */

fun getSavedLocaleDisplayName(context: android.content.Context): String {
    val prefs = context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE)
    val langTag = prefs.getString("app_language", "system") ?: "system"
    if (langTag == "system") return context.getString(R.string.follow_system)
    return when (langTag) {
        "en" -> "English"
        "ar" -> "العربية"
        "az" -> "Azərbaycan"
        "be" -> "Беларуская"
        "zh-Hans" -> "简体中文"
        "zh-Hant" -> "繁體中文"
        "hr" -> "Hrvatski"
        "cs" -> "Čeština"
        "da" -> "Dansk"
        "nl" -> "Nederlands"
        "fil" -> "Filipino"
        "fr" -> "Français"
        "de" -> "Deutsch"
        "el" -> "Ελληνικά"
        "hi" -> "हिन्दी"
        "hu" -> "Magyar"
        "in" -> "Bahasa Indonesia"
        "it" -> "Italiano"
        "ja" -> "日本語"
        "ko" -> "한국어"
        "ms" -> "Bahasa Melayu"
        "mn" -> "Монгол"
        "fa" -> "فارسی"
        "pl" -> "Polski"
        "pt" -> "Português"
        "ru" -> "Русский"
        "sr" -> "Српски"
        "si" -> "සිංහල"
        "es" -> "Español"
        "sv" -> "Svenska"
        "th" -> "ไทย"
        "tr" -> "Türkçe"
        "uk" -> "Українська"
        "vi" -> "Tiếng Việt"
        "zgh" -> "ⵜⴰⵎⴰⵣⵉⵖⵜ"
        else -> Locale.forLanguageTag(langTag).getDisplayName(Locale.forLanguageTag(langTag)).replaceFirstChar { it.uppercase() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferences(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }

    var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", 0)) }
    var isDynamicColor by remember { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "dark_theme") {
                darkThemePref = p.getInt("dark_theme", 0)
            } else if (key == "dynamic_color") {
                isDynamicColor = p.getBoolean("dynamic_color", true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isDark = when (darkThemePref) {
        1 -> true
        2 -> false
        else -> isSystemInDarkTheme()
    }

    val darkThemeDesc = when (darkThemePref) {
        1 -> stringResource(id = R.string.on)
        2 -> stringResource(id = R.string.off)
        else -> stringResource(id = R.string.follow_system)
    }

    BasePreferencePage(
        title = stringResource(id = R.string.look_and_feel),
        onBack = onNavigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PreferenceSwitch(
                title = stringResource(id = R.string.dynamic_color),
                description = stringResource(id = R.string.dynamic_color_desc),
                icon = Icons.Outlined.Colorize,
                isChecked = isDynamicColor,
                onClick = {
                    val newValue = !isDynamicColor
                    isDynamicColor = newValue
                    prefs.edit().putBoolean("dynamic_color", newValue).apply()
                },
            )
            PreferenceSwitchWithDivider(
                title = stringResource(id = R.string.dark_theme),
                icon = if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                isChecked = isDark,
                description = darkThemeDesc,
                onChecked = {
                    val newPref = if (isDark) 2 else 1
                    darkThemePref = newPref
                    prefs.edit().putInt("dark_theme", newPref).apply()
                },
                onClick = { onNavigateTo("dark_theme") },
            )
            PreferenceItem(
                title = stringResource(R.string.language),
                icon = Icons.Outlined.Language,
                description = getSavedLocaleDisplayName(context),
            ) {
                onNavigateTo("languages")
            }
        }
    }
}

/* ---------------- DarkThemePreferences ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkThemePreferences(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }
    var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", 0)) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.dark_theme),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 48.dp
                )
            ) {
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    item {
                        PreferenceSingleChoiceItem(
                            text = stringResource(R.string.follow_system),
                            selected = darkThemePref == 0,
                            onClick = {
                                darkThemePref = 0
                                prefs.edit().putInt("dark_theme", 0).apply()
                            }
                        )
                    }
                }
                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(R.string.on),
                        selected = darkThemePref == 1,
                        onClick = {
                            darkThemePref = 1
                            prefs.edit().putInt("dark_theme", 1).apply()
                        }
                    )
                }
                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(R.string.off),
                        selected = darkThemePref == 2,
                        onClick = {
                            darkThemePref = 2
                            prefs.edit().putInt("dark_theme", 2).apply()
                        }
                    )
                }
            }
        }
    )
}

/* ---------------- LanguagesPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesPage(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }
    var selectedLangTag by remember { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }

    val suggestedLanguages = remember {
        listOf(
            Triple("English", "en", Locale.ENGLISH),
            Triple("العربية", "ar", Locale.forLanguageTag("ar")),
        )
    }

    val allLanguagesList = remember {
        listOf(
            Triple("العربية", "ar", Locale.forLanguageTag("ar")),
            Triple("Azərbaycan", "az", Locale.forLanguageTag("az")),
            Triple("Беларуская", "be", Locale.forLanguageTag("be")),
            Triple("简体中文", "zh-Hans", Locale.forLanguageTag("zh-Hans")),
            Triple("繁體中文", "zh-Hant", Locale.forLanguageTag("zh-Hant")),
            Triple("Hrvatski", "hr", Locale.forLanguageTag("hr")),
            Triple("Čeština", "cs", Locale.forLanguageTag("cs")),
            Triple("Dansk", "da", Locale.forLanguageTag("da")),
            Triple("Nederlands", "nl", Locale.forLanguageTag("nl")),
            Triple("English", "en", Locale.ENGLISH),
            Triple("Filipino", "fil", Locale.forLanguageTag("fil")),
            Triple("Français", "fr", Locale.FRENCH),
            Triple("Deutsch", "de", Locale.GERMAN),
            Triple("Ελληνικά", "el", Locale.forLanguageTag("el")),
            Triple("हिन्दी", "hi", Locale.forLanguageTag("hi")),
            Triple("Magyar", "hu", Locale.forLanguageTag("hu")),
            Triple("Bahasa Indonesia", "in", Locale.forLanguageTag("in")),
            Triple("Italiano", "it", Locale.ITALIAN),
            Triple("日本語", "ja", Locale.JAPANESE),
            Triple("한국어", "ko", Locale.KOREAN),
            Triple("Bahasa Melayu", "ms", Locale.forLanguageTag("ms")),
            Triple("Монгол", "mn", Locale.forLanguageTag("mn")),
            Triple("فارسی", "fa", Locale.forLanguageTag("fa")),
            Triple("Polski", "pl", Locale.forLanguageTag("pl")),
            Triple("Português", "pt", Locale.forLanguageTag("pt")),
            Triple("Русский", "ru", Locale.forLanguageTag("ru")),
            Triple("Српски", "sr", Locale.forLanguageTag("sr")),
            Triple("සිංහල", "si", Locale.forLanguageTag("si")),
            Triple("Español", "es", Locale.forLanguageTag("es")),
            Triple("Svenska", "sv", Locale.forLanguageTag("sv")),
            Triple("ไทย", "th", Locale.forLanguageTag("th")),
            Triple("Türkçe", "tr", Locale.forLanguageTag("tr")),
            Triple("Українська", "uk", Locale.forLanguageTag("uk")),
            Triple("Tiếng Việt", "vi", Locale.forLanguageTag("vi")),
            Triple("ⵜⴰⵎⴰⵣⵉⵖⵜ", "zgh", Locale.forLanguageTag("zgh"))
        )
    }

    val deviceLocale = remember {
        ConfigurationCompat.getLocales(context.resources.configuration).get(0) ?: Locale.getDefault()
    }

    val isSystemLangSupported = remember(deviceLocale, allLanguagesList) {
        val devTag = deviceLocale.toLanguageTag().lowercase()
        val devLang = deviceLocale.language.lowercase()
        allLanguagesList.any { (_, tag, _) ->
            val t = tag.lowercase()
            t == devTag || t == devLang || devTag.startsWith(t) || devLang == t.split("-")[0]
        }
    }

    fun setAppLanguage(langTag: String, locale: Locale?) {
        selectedLangTag = langTag
        prefs.edit().putString("app_language", langTag).apply()

        val localeListCompat = if (langTag == "system" || locale == null) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(langTag)
        }
        AppCompatDelegate.setApplicationLocales(localeListCompat)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.content.Context.LOCALE_SERVICE) as? LocaleManager
            if (localeManager != null) {
                val localeList = if (langTag == "system" || locale == null) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList(locale)
                }
                localeManager.applicationLocales = localeList
            }
        }

        val targetLocale = if (langTag == "system" || locale == null) Locale.getDefault() else locale
        Locale.setDefault(targetLocale)
        val config = context.resources.configuration
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val typography = MaterialTheme.typography
            val overrideTypography = remember(typography) {
                typography.copy(headlineMedium = typography.displaySmall)
            }
            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.language), color = MaterialTheme.colorScheme.onBackground)
                    },
                    navigationIcon = { BackButton { onNavigateBack() } },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp
                )
            ) {
                item {
                    PreferencesHintCard(
                        title = stringResource(id = R.string.translate),
                        description = stringResource(id = R.string.translate_desc),
                        icon = Icons.Outlined.Translate,
                    ) {
                        uriHandler.openUri("https://github.com/hamzabellouch/apex")
                    }
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.suggested))
                }

                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(id = R.string.follow_system),
                        selected = selectedLangTag == "system",
                        selectedColor = if (!isSystemLangSupported) androidx.compose.ui.graphics.Color(0xFFE53935) else null,
                        onClick = { setAppLanguage("system", null) },
                    )
                }

                items(suggestedLanguages) { (displayName, langTag, locale) ->
                    PreferenceSingleChoiceItem(
                        text = displayName,
                        selected = selectedLangTag == langTag,
                        onClick = { setAppLanguage(langTag, locale) },
                    )
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.all_languages))
                }

                items(allLanguagesList) { (displayName, langTag, locale) ->
                    PreferenceSingleChoiceItem(
                        text = displayName,
                        selected = selectedLangTag == langTag,
                        onClick = { setAppLanguage(langTag, locale) },
                    )
                }
            }
        },
    )
}

@Composable
fun Conversation(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/* ---------------- SponsorsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorsPage(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true },
    )
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val typography = MaterialTheme.typography
            val overrideTypography = remember(typography) {
                typography.copy(headlineMedium = typography.displaySmall)
            }

            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.sponsors), color = MaterialTheme.colorScheme.onBackground)
                    },
                    navigationIcon = { BackButton { onNavigateBack() } },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        content = { values ->
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 12.dp),
                columns = GridCells.Fixed(12),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = values,
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = CardDefaults.shape,
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = stringResource(id = R.string.msg_from_developer),
                                style = MaterialTheme.typography.labelLarge,
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.developer_avatar),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .aspectRatio(1f, true)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Conversation(
                                        modifier = Modifier.padding(bottom = 12.dp),
                                        text = stringResource(id = R.string.sponsor_msg),
                                    )
                                    Conversation(
                                        modifier = Modifier,
                                        text = stringResource(id = R.string.sponsor_msg2),
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    uriHandler.openUri("https://github.com/sponsors/hamzabellouch")
                                },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(ButtonDefaults.IconSize),
                                    imageVector = Icons.Outlined.VolunteerActivism,
                                    contentDescription = null,
                                )

                                Text(text = stringResource(id = R.string.sponsor))
                            }
                        }
                    }
                }
            }
        },
    )
}

/* ---------------- TroubleShootingPage ---------------- */

@Composable
fun TroubleShootingPage(onNavigateBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showContactDialog by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }
    var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", 0)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "dark_theme") {
                darkThemePref = p.getInt("dark_theme", 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isDark = when (darkThemePref) {
        1 -> true
        2 -> false
        else -> isSystemInDarkTheme()
    }

    val emailContainer = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val emailContent = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    val facebookContainer = if (isDark) Color(0xFF0D2646) else Color(0xFFE7F3FF)
    val facebookContent = if (isDark) Color(0xFF4599FF) else Color(0xFF1877F2)

    val instagramContainer = if (isDark) Color(0xFF3D1625) else Color(0xFFFDF0F3)
    val instagramContent = if (isDark) Color(0xFFFF527B) else Color(0xFFD82E62)

    val linkedinContainer = if (isDark) Color(0xFF0E2E4E) else Color(0xFFE8F2FF)
    val linkedinContent = if (isDark) Color(0xFF55A4FC) else Color(0xFF0A66C2)

    val xContainer = if (isDark) Color(0xFF16181C) else Color(0xFFF5F8FA)
    val xContent = if (isDark) Color(0xFFE7E9EA) else Color(0xFF0F1419)

    val youtubeContainer = if (isDark) Color(0xFF3A1115) else Color(0xFFFFEBEE)
    val youtubeContent = if (isDark) Color(0xFFE53935) else Color(0xFFCC0000)

    val tiktokContainer = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F1F1)
    val tiktokContent = if (isDark) Color(0xFFFFFFFF) else Color(0xFF010101)

    val redditContainer = if (isDark) Color(0xFF3D1E16) else Color(0xFFFFEBE5)
    val redditContent = if (isDark) Color(0xFFFF5A1F) else Color(0xFFFF4500)

    val blueskyContainer = if (isDark) Color(0xFF0A2E4C) else Color(0xFFE8F8FF)
    val blueskyContent = if (isDark) Color(0xFF3BA1FF) else Color(0xFF0085FF)

    val telegramContainer = if (isDark) Color(0xFF0F2C3D) else Color(0xFFE8F5FA)
    val telegramContent = if (isDark) Color(0xFF52B6E9) else Color(0xFF24A1DE)

    BasePreferencePage(
        title = stringResource(R.string.trouble_shooting),
        onBack = onNavigateBack,
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            item {
                val pagerState = rememberPagerState(initialPage = 0) { 10 }
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        when (page) {
                            0 -> PreferencesHintCard(
                                title = stringResource(R.string.contact),
                                description = stringResource(R.string.contact_desc),
                                icon = Icons.Outlined.Email,
                                containerColor = emailContainer,
                                contentColor = emailContent,
                                textColor = Color.White,
                            ) { showContactDialog = true }

                            1 -> PreferencesHintCard(
                                title = stringResource(id = R.string.facebook),
                                icon = painterResource(id = R.drawable.ic_facebook),
                                description = stringResource(id = R.string.facebook_desc),
                                containerColor = facebookContainer,
                                contentColor = facebookContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.facebook.com/hamzabellouch1") }

                            2 -> PreferencesHintCard(
                                title = stringResource(id = R.string.instagram),
                                icon = painterResource(id = R.drawable.ic_instagram),
                                description = stringResource(id = R.string.instagram_desc),
                                containerColor = instagramContainer,
                                contentColor = instagramContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.instagram.com/hamzabellouch0") }

                            3 -> PreferencesHintCard(
                                title = stringResource(id = R.string.linkedin),
                                icon = painterResource(id = R.drawable.ic_linkedin),
                                description = stringResource(id = R.string.linkedin_desc),
                                containerColor = linkedinContainer,
                                contentColor = linkedinContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.linkedin.com/in/hamzabellouch") }

                            4 -> PreferencesHintCard(
                                title = stringResource(id = R.string.x_platform),
                                icon = painterResource(id = R.drawable.ic_x),
                                description = stringResource(id = R.string.x_desc),
                                containerColor = xContainer,
                                contentColor = xContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://x.com/hamzabellouch0") }

                            5 -> PreferencesHintCard(
                                title = stringResource(id = R.string.youtube),
                                icon = painterResource(id = R.drawable.ic_youtube),
                                description = stringResource(id = R.string.youtube_desc),
                                containerColor = youtubeContainer,
                                contentColor = youtubeContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.youtube.com/@hamzabellouch") }

                            6 -> PreferencesHintCard(
                                title = stringResource(id = R.string.tiktok),
                                icon = painterResource(id = R.drawable.ic_tiktok),
                                description = stringResource(id = R.string.tiktok_desc),
                                containerColor = tiktokContainer,
                                contentColor = tiktokContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.tiktok.com/@hamzabellouch0") }

                            7 -> PreferencesHintCard(
                                title = stringResource(id = R.string.reddit),
                                icon = painterResource(id = R.drawable.ic_reddit),
                                description = stringResource(id = R.string.reddit_desc),
                                containerColor = redditContainer,
                                contentColor = redditContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.reddit.com") }

                            8 -> PreferencesHintCard(
                                title = stringResource(id = R.string.bluesky),
                                icon = painterResource(id = R.drawable.ic_bluesky),
                                description = stringResource(id = R.string.bluesky_desc),
                                containerColor = blueskyContainer,
                                contentColor = blueskyContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://bsky.app/profile/hamzabellouch.bsky.social") }

                            9 -> PreferencesHintCard(
                                title = stringResource(id = R.string.telegram_channel),
                                icon = painterResource(id = R.drawable.icons8_telegram_app),
                                description = stringResource(id = R.string.telegram_channel_desc),
                                containerColor = telegramContainer,
                                contentColor = telegramContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://t.me/hamzabellouch") }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(10) { pageIndex ->
                            val isSelected = pagerState.currentPage == pageIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                }
            }
            item {
                OutlinedCard(modifier = Modifier.padding(16.dp)) {
                    PreferenceInfo(
                        modifier = Modifier,
                        text = stringResource(R.string.issue_tracker_hint),
                    )
                    PreferenceItem(
                        title = stringResource(R.string.links_issue_tracker),
                        description = null,
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { uriHandler.openUri("https://github.com/hamzabellouch/apex/issues") },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            confirmButton = {
                FilledButtonWithIcon(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    text = stringResource(id = R.string.proceed),
                    onClick = {
                        showContactDialog = false
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:hamzabellouchcontact@gmail.com")
                            setPackage("com.google.android.gm")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:hamzabellouchcontact@gmail.com")
                            }
                            try {
                                context.startActivity(Intent.createChooser(fallbackIntent, "Send Email"))
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "No email app found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                )
            },
            dismissButton = {
                OutlinedButtonWithIcon(
                    icon = Icons.Outlined.Cancel,
                    text = stringResource(id = R.string.cancel),
                    onClick = { showContactDialog = false },
                )
            },
            title = { Text(text = stringResource(R.string.contact_developer)) },
            text = { Text(text = stringResource(R.string.contact_developer_confirm)) },
        )
    }
}

/* ---------------- AboutPage ---------------- */

private const val releaseURL = "https://github.com/hamzabellouch/apex/releases"
private const val repoUrl = "https://github.com/hamzabellouch/apex/blob/main/README.md"
private const val githubIssueUrl = "https://github.com/hamzabellouch/apex/issues"
private const val matrixSpaceUrl = "https://sites.google.com/view/hamzabellouch"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    onNavigateBack: () -> Unit,
    onNavigateToCreditsPage: () -> Unit,
    onNavigateToUpdatePage: () -> Unit = {},
    onNavigateToDonatePage: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("apex_prefs", android.content.Context.MODE_PRIVATE) }
    var isAutoUpdateEnabled by remember {
        mutableStateOf(prefs.getBoolean("auto_update_enabled", false))
    }

    // Launch background update check
    AppUpdater(isAutoUpdateEnabled = isAutoUpdateEnabled)

    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.6-beta"
    } catch (e: Exception) {
        "0.0.6-beta"
    }
    val info = "App version: $versionName\nPackage name: ${context.packageName}\nDevice: Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
    val uriHandler = LocalUriHandler.current

    fun openUrl(url: String) {
        uriHandler.openUri(url)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val typography = MaterialTheme.typography
            val overrideTypography =
                remember(typography) { typography.copy(headlineMedium = typography.displaySmall) }

            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = {
                        Text(modifier = Modifier, text = stringResource(id = R.string.about), color = MaterialTheme.colorScheme.onBackground)
                    },
                    navigationIcon = { BackButton { onNavigateBack() } },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    PreferenceItem(
                        title = stringResource(R.string.readme),
                        description = stringResource(R.string.readme_desc),
                        icon = Icons.Outlined.Description,
                    ) {
                        openUrl(repoUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.release),
                        description = stringResource(R.string.release_desc),
                        icon = Icons.Outlined.NewReleases,
                    ) {
                        openUrl(releaseURL)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.github_issue),
                        description = stringResource(R.string.github_issue_desc),
                        icon = Icons.AutoMirrored.Outlined.ContactSupport,
                    ) {
                        openUrl(githubIssueUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.github_stars),
                        description = stringResource(R.string.github_stars_desc),
                        icon = Icons.Outlined.StarBorder,
                    ) {
                        openUrl("https://github.com/hamzabellouch/apex")
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.website),
                        description = matrixSpaceUrl,
                        icon = Icons.Outlined.Language,
                    ) {
                        openUrl(matrixSpaceUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.credits),
                        description = stringResource(id = R.string.credits_desc),
                        icon = Icons.Outlined.AutoAwesome,
                    ) {
                        onNavigateToCreditsPage()
                    }
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.auto_update),
                        description = stringResource(R.string.check_for_updates_desc),
                        icon =
                            if (isAutoUpdateEnabled) Icons.Outlined.Update
                            else Icons.Outlined.UpdateDisabled,
                        isChecked = isAutoUpdateEnabled,
                        isSwitchEnabled = true,
                        onClick = onNavigateToUpdatePage,
                        onChecked = {
                            isAutoUpdateEnabled = !isAutoUpdateEnabled
                            prefs.edit().putBoolean("auto_update_enabled", isAutoUpdateEnabled).apply()
                        },
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.version),
                        description = versionName,
                        icon = Icons.Outlined.Info,
                    ) {
                        coroutineScope.launch {
                            clipboard.setClipEntry(ClipEntry(android.content.ClipData.newPlainText("info", info)))
                        }
                        android.widget.Toast.makeText(context, context.getString(R.string.info_copied), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.package_name),
                        description = context.packageName,
                        icon = Icons.Outlined.Code,
                    ) {
                        coroutineScope.launch {
                            clipboard.setClipEntry(ClipEntry(android.content.ClipData.newPlainText("package_name", context.packageName)))
                        }
                        android.widget.Toast.makeText(context, context.getString(R.string.info_copied), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
    )
}

/* ---------------- CreditsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsPage(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val creditsList = remember {
        listOf(
            Triple("ReadYou", "GPL-3.0 License", "https://github.com/Ashinch/ReadYou"),
            Triple("Android Jetpack", "Apache License, Version 2.0", "https://github.com/androidx/androidx"),
            Triple("Kotlin", "Apache License, Version 2.0", "https://github.com/JetBrains/kotlin"),
            Triple("kotlinx.serialization", "Apache License, Version 2.0", "https://github.com/Kotlin/kotlinx.serialization"),
            Triple("OkHttp", "Apache License, Version 2.0", "https://github.com/square/okhttp"),
            Triple("Material Design 3", "Apache License, Version 2.0", "https://github.com/material-components/material-components-android"),
            Triple("Material Icons", "Apache License, Version 2.0", "https://fonts.google.com/icons"),
            Triple("Accompanist", "Apache License, Version 2.0", "https://github.com/google/accompanist"),
            Triple("ZXing", "Apache License, Version 2.0", "https://github.com/zxing/zxing"),
            Triple("App icon by Icons8", "Universal Multimedia Licensing Agreement for Icons8", "https://icons8.com/"),
        )
    }

    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.credits),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .clip(MaterialTheme.shapes.large)
                            .clickable {}
                            .clearAndSetSemantics {},
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        val painter = rememberVectorPainter(image = DynamicColorImageVectors.coder())
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 72.dp, vertical = 48.dp),
                        )
                    }
                }
                items(creditsList) { item ->
                    CreditItem(title = item.first, license = item.second) {
                        uriHandler.openUri(item.third)
                    }
                }
            }
        },
    )
}
