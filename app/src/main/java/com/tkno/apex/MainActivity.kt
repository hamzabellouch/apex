package com.tkno.apex

import android.app.LocaleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tkno.apex.ui.common.LocalDarkTheme
import com.tkno.apex.util.PreferenceUtil
import com.tkno.apex.util.DarkThemePreference
import com.tkno.apex.service.CacheCleanerAccessibilityService
import com.tkno.apex.service.ManualOperationManager
import com.tkno.apex.theme.ApexTheme
import com.tkno.apex.ui.main.MainScreen
import com.tkno.apex.ui.onboarding.OnboardingScreen
import com.tkno.apex.util.AppStorageHelper
import com.tkno.apex.util.LanguageManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.TextUtilsCompat
import androidx.core.os.ConfigurationCompat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var isInPipMode by mutableStateOf(false)
    private var wasInPipMode = false

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            wasInPipMode = true
        } else {
            if (wasInPipMode) {
                wasInPipMode = false
                if (CacheCleanerAccessibilityService.isRunning) {
                    CacheCleanerAccessibilityService.stopCleaning(returnToApp = true)
                }
                if (ManualOperationManager.isRunning) {
                    ManualOperationManager.stop(this, returnToApp = true)
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
        val langTag = prefs.getString("app_language", "system") ?: "system"
        if (langTag != "system") {
            val localizedContext = LanguageManager.wrapContext(newBase, langTag)
            super.attachBaseContext(localizedContext)
            return
        }
        super.attachBaseContext(newBase)
    }

    private var isUsageAccessGranted by mutableStateOf(false)
    private var isAccessibilityEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceUtil.init(this)
        applySavedLocale()
        applyInitialWindowBackground()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        disableAutoPip()

        isUsageAccessGranted = AppStorageHelper.isUsageStatsPermissionGranted(this)
        isAccessibilityEnabled = isAccessibilityServiceEnabled(this)
        val prefs = getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)

        setContent {
            val context = LocalContext.current
            val darkThemePreference by PreferenceUtil.darkThemeFlow.collectAsStateWithLifecycle()
            var isDynamicColor by remember { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }
            var appLanguagePref by remember { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }

            DisposableEffect(prefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == "dynamic_color") {
                        isDynamicColor = p.getBoolean("dynamic_color", true)
                    } else if (key == "app_language") {
                        appLanguagePref = p.getString("app_language", "system") ?: "system"
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val localeContext = remember(appLanguagePref, context) {
                LanguageManager.wrapContext(context, appLanguagePref)
            }
            val currentLocale = remember(appLanguagePref) {
                if (appLanguagePref == "system") {
                    ConfigurationCompat.getLocales(android.content.res.Resources.getSystem().configuration).get(0)
                        ?: Locale.getDefault()
                } else {
                    val custom = LanguageManager.loadCustomLanguage(context, appLanguagePref)
                    if (custom != null) {
                        val cleanTag = when {
                            custom.code.startsWith("b+") -> custom.code.removePrefix("b+").replace('+', '-')
                            else -> custom.code
                        }
                        Locale.forLanguageTag(cleanTag)
                    } else {
                        Locale.forLanguageTag(appLanguagePref)
                    }
                }
            }
            val isRtl = remember(currentLocale, appLanguagePref) {
                val custom = LanguageManager.loadCustomLanguage(context, appLanguagePref)
                if (custom != null) {
                    custom.direction.equals("rtl", ignoreCase = true)
                } else {
                    TextUtilsCompat.getLayoutDirectionFromLocale(currentLocale) == android.view.View.LAYOUT_DIRECTION_RTL
                }
            }
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localeContext,
                LocalLayoutDirection provides layoutDirection,
                LocalDarkTheme provides darkThemePreference
            ) {
                ApexTheme(
                    darkTheme = darkThemePreference.isDarkTheme(),
                    isHighContrastModeEnabled = darkThemePreference.isHighContrastModeEnabled,
                    dynamicColor = isDynamicColor
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                    var isOnboardingCompleted by remember {
                        mutableStateOf(prefs.getBoolean("is_onboarding_completed", false))
                    }

                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            onOnboardingFinished = {
                                prefs.edit().putBoolean("is_onboarding_completed", true).apply()
                                isOnboardingCompleted = true
                            }
                        )
                    } else {
                        MainScreen(
                            isUsageAccessGranted = isUsageAccessGranted,
                            isAccessibilityEnabled = isAccessibilityEnabled,
                            isInPipMode = isInPipMode,
                            onEnterPip = { triggerPipMode() },
                            onRequestUsageAccess = {
                                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            },
                            onRequestAccessibility = {
                                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }
                        )
                    }
                }
            }
        }
    }
}

    override fun onResume() {
        super.onResume()
        disableAutoPip()
        
        // Refresh permission states on resume without destroying Compose tree
        isUsageAccessGranted = AppStorageHelper.isUsageStatsPermissionGranted(this)
        isAccessibilityEnabled = isAccessibilityServiceEnabled(this)
    }

    private fun disableAutoPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val builder = android.app.PictureInPictureParams.Builder()
                    .setAutoEnterEnabled(false)
                    .setAspectRatio(android.util.Rational(200, 100))
                setPictureInPictureParams(builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun triggerPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val builder = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(200, 100))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(false)
                }
                enterPictureInPictureMode(builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyInitialWindowBackground() {
        val prefs = getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
        val darkThemePref = prefs.getInt("dark_theme", DarkThemePreference.FOLLOW_SYSTEM)
        val isHighContrast = prefs.getBoolean("high_contrast_mode", false)
        val systemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val useDarkTheme = when (darkThemePref) {
            DarkThemePreference.ON -> true
            DarkThemePreference.OFF -> false
            else -> systemDark
        }
        val bgColor = if (useDarkTheme) {
            if (isHighContrast) android.graphics.Color.BLACK else android.graphics.Color.parseColor("#0E141D")
        } else {
            android.graphics.Color.WHITE
        }
        val bgDrawable = android.graphics.drawable.ColorDrawable(bgColor)
        window.setBackgroundDrawable(bgDrawable)
    }

    private fun applySavedLocale() {
        val prefs = getSharedPreferences("apex_prefs", Context.MODE_PRIVATE)
        val langTag = prefs.getString("app_language", "system") ?: "system"

        val isBuiltIn = LanguageManager.isBuiltInLanguage(langTag)
        if (isBuiltIn) {
            val localeListCompat = if (langTag == "system") {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(langTag)
            }
            AppCompatDelegate.setApplicationLocales(localeListCompat)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
                if (localeManager != null) {
                    val localeList = if (langTag == "system") {
                        LocaleList.getEmptyLocaleList()
                    } else {
                        LocaleList(Locale.forLanguageTag(langTag))
                    }
                    localeManager.applicationLocales = localeList
                }
            }
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
                localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
            }
        }

        val custom = if (langTag != "system") LanguageManager.loadCustomLanguage(this, langTag) else null
        val targetLocale = if (langTag == "system") {
            ConfigurationCompat.getLocales(android.content.res.Resources.getSystem().configuration).get(0)
                ?: Locale.getDefault()
        } else if (custom != null) {
            val cleanTag = when {
                custom.code.startsWith("b+") -> custom.code.removePrefix("b+").replace('+', '-')
                else -> custom.code
            }
            Locale.forLanguageTag(cleanTag)
        } else {
            Locale.forLanguageTag(langTag)
        }

        Locale.setDefault(targetLocale)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        AppStorageHelper.clearAllMemoryCaches()
    }

    override fun onStop() {
        super.onStop()
        AppStorageHelper.clearAllMemoryCaches()
        if ((isFinishing || wasInPipMode) && CacheCleanerAccessibilityService.isRunning) {
            CacheCleanerAccessibilityService.stopCleaning(returnToApp = true)
        }
        if ((isFinishing || wasInPipMode) && ManualOperationManager.isRunning) {
            ManualOperationManager.stop(this, returnToApp = true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppStorageHelper.clearAllMemoryCaches()
        if (CacheCleanerAccessibilityService.isRunning) {
            CacheCleanerAccessibilityService.stopCleaning(returnToApp = false)
        }
        if (ManualOperationManager.isRunning) {
            ManualOperationManager.stop(this, returnToApp = false)
        }
        if (isFinishing) {
            android.os.Process.killProcess(android.os.Process.myUid())
            kotlin.system.exitProcess(0)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            AppStorageHelper.clearAllMemoryCaches()
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, CacheCleanerAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
    }
}
