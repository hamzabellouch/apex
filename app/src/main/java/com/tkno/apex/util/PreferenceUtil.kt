package com.tkno.apex.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tkno.apex.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DarkThemePreference(
    val darkThemeValue: Int = FOLLOW_SYSTEM,
    val isHighContrastModeEnabled: Boolean = false,
) {
    companion object {
        const val FOLLOW_SYSTEM = 1
        const val ON = 2
        const val OFF = 3
    }

    @Composable
    fun isDarkTheme(): Boolean {
        return if (darkThemeValue == FOLLOW_SYSTEM) isSystemInDarkTheme() else darkThemeValue == ON
    }

    @Composable
    fun getDarkThemeDesc(): String {
        return when (darkThemeValue) {
            FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
            ON -> stringResource(R.string.on)
            else -> stringResource(R.string.off)
        }
    }
}

object PreferenceUtil {
    private const val PREFS_NAME = "apex_prefs"
    const val KEY_DARK_THEME = "dark_theme"
    const val KEY_HIGH_CONTRAST = "high_contrast_mode"

    private var sharedPreferences: SharedPreferences? = null

    private val _darkThemeFlow = MutableStateFlow(DarkThemePreference())
    val darkThemeFlow = _darkThemeFlow.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences = prefs

            val rawDark = prefs.getInt(KEY_DARK_THEME, DarkThemePreference.FOLLOW_SYSTEM)
            val darkThemeValue = when (rawDark) {
                0 -> DarkThemePreference.FOLLOW_SYSTEM
                DarkThemePreference.FOLLOW_SYSTEM -> DarkThemePreference.FOLLOW_SYSTEM
                DarkThemePreference.ON -> DarkThemePreference.ON
                DarkThemePreference.OFF -> DarkThemePreference.OFF
                else -> DarkThemePreference.FOLLOW_SYSTEM
            }
            val isHighContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false)

            _darkThemeFlow.value = DarkThemePreference(
                darkThemeValue = darkThemeValue,
                isHighContrastModeEnabled = isHighContrast
            )
        }
    }

    fun modifyDarkThemePreference(
        darkThemeValue: Int = _darkThemeFlow.value.darkThemeValue,
        isHighContrastModeEnabled: Boolean = _darkThemeFlow.value.isHighContrastModeEnabled,
    ) {
        _darkThemeFlow.update {
            it.copy(
                darkThemeValue = darkThemeValue,
                isHighContrastModeEnabled = isHighContrastModeEnabled
            )
        }
        sharedPreferences?.edit()?.apply {
            putInt(KEY_DARK_THEME, darkThemeValue)
            putBoolean(KEY_HIGH_CONTRAST, isHighContrastModeEnabled)
            apply()
        }
    }
}
