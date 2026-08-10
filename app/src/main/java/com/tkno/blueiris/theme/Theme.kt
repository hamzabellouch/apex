package com.tkno.blueiris.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.ConfigurationCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import java.util.Locale

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF48AFFF),
    secondary = Color(0xFF35C759),
    tertiary = Color(0xFFFF9500),
    background = Color(0xFF090D10),
    surface = Color(0xFF090D10),
    surfaceContainer = Color(0xFF10151B),
    surfaceContainerHigh = Color(0xFF16202B),
    surfaceContainerHighest = Color(0xFF1E2B37),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF8E98A0),
    outline = Color(0xFF263342),
    outlineVariant = Color(0xFF1E2B37)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0066FF),
    secondary = Color(0xFF28CD41),
    tertiary = Color(0xFFFF9500),
    background = Color(0xFFF4F6F9),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFEBF0F5),
    surfaceContainerHighest = Color(0xFFE2E7EC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB)
)

@Composable
fun BlueIrisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val prefs = remember(context) { context.getSharedPreferences("blueiris_prefs", Context.MODE_PRIVATE) }
    var langTag by remember { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "app_language") {
                langTag = p.getString("app_language", "system") ?: "system"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val currentLocale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    }

    val targetLocale = remember(langTag, currentLocale) {
        if (langTag == "system") currentLocale else Locale.forLanguageTag(langTag)
    }

    val isRtl = remember(targetLocale) {
        TextUtilsCompat.getLayoutDirectionFromLocale(targetLocale) == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val isArabic = remember(targetLocale) {
        targetLocale.language == "ar"
    }

    val updatedConfig = remember(configuration, targetLocale, isRtl) {
        android.content.res.Configuration(configuration).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        }
    }

    val updatedContext = remember(context, updatedConfig) {
        context.createConfigurationContext(updatedConfig)
    }

    val typography = remember(isArabic) {
        getAppTypography(isArabic)
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalConfiguration provides updatedConfig,
        LocalContext provides updatedContext,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
    }
}

