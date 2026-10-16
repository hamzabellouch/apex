package com.tkno.apex.ui.common

import androidx.compose.runtime.compositionLocalOf
import com.tkno.apex.util.DarkThemePreference

val LocalDarkTheme = compositionLocalOf { DarkThemePreference() }
