package com.tkno.apex.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tkno.apex.R

val NotoSansArabic = FontFamily(
    Font(R.font.noto_sans_arabic, FontWeight.Normal),
    Font(R.font.noto_sans_arabic, FontWeight.Medium),
    Font(R.font.noto_sans_arabic, FontWeight.SemiBold),
    Font(R.font.noto_sans_arabic, FontWeight.Bold)
)

fun getAppTypography(isArabic: Boolean): Typography {
    val fontFamily = if (isArabic) NotoSansArabic else FontFamily.Default
    val defaultTypography = Typography()

    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(
            fontFamily = fontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
    )
}

val Typography = getAppTypography(isArabic = false)

