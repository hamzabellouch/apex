package com.tkno.blueiris

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
import com.tkno.blueiris.service.CacheCleanerAccessibilityService
import com.tkno.blueiris.theme.BlueIrisTheme
import com.tkno.blueiris.ui.main.MainScreen
import com.tkno.blueiris.ui.onboarding.OnboardingScreen
import com.tkno.blueiris.util.AppStorageHelper
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("blueiris_prefs", Context.MODE_PRIVATE)
        val langTag = prefs.getString("app_language", "system") ?: "system"
        if (langTag != "system") {
            val locale = Locale.forLanguageTag(langTag)
            Locale.setDefault(locale)
            val config = newBase.resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            val context = newBase.createConfigurationContext(config)
            super.attachBaseContext(context)
            return
        }
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedLocale()
        applyInitialWindowBackground()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    private fun applyInitialWindowBackground() {
        val prefs = getSharedPreferences("blueiris_prefs", Context.MODE_PRIVATE)
        val darkThemePref = prefs.getInt("dark_theme", 0)
        val systemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val useDarkTheme = when (darkThemePref) {
            1 -> true
            2 -> false
            else -> systemDark
        }
        val bgDrawable = android.graphics.drawable.ColorDrawable(
            if (useDarkTheme) android.graphics.Color.parseColor("#0E141D") else android.graphics.Color.WHITE
        )
        window.setBackgroundDrawable(bgDrawable)
    }

    private fun applySavedLocale() {
        val prefs = getSharedPreferences("blueiris_prefs", Context.MODE_PRIVATE)
        val langTag = prefs.getString("app_language", "system") ?: "system"

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

        val targetLocale = if (langTag == "system") Locale.getDefault() else Locale.forLanguageTag(langTag)
        Locale.setDefault(targetLocale)
        val config = resources.configuration
        config.setLocale(targetLocale)
        config.setLayoutDirection(targetLocale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        
        // Refresh UI with permission states on resume
        val isUsageAccessGranted = AppStorageHelper.isUsageStatsPermissionGranted(this)
        val isAccessibilityEnabled = isAccessibilityServiceEnabled(this)
        val prefs = getSharedPreferences("blueiris_prefs", Context.MODE_PRIVATE)

        setContent {
            var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", 0)) }
            var isDynamicColor by remember { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }
            var appLanguagePref by remember { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }

            DisposableEffect(prefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == "dark_theme") {
                        darkThemePref = p.getInt("dark_theme", 0)
                    } else if (key == "dynamic_color") {
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

            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (darkThemePref) {
                1 -> true
                2 -> false
                else -> systemDark
            }

            BlueIrisTheme(
                darkTheme = useDarkTheme,
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

    override fun onPause() {
        super.onPause()
        AppStorageHelper.clearAllMemoryCaches()
    }

    override fun onStop() {
        super.onStop()
        AppStorageHelper.clearAllMemoryCaches()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppStorageHelper.clearAllMemoryCaches()
        if (isFinishing && !CacheCleanerAccessibilityService.isRunning) {
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
