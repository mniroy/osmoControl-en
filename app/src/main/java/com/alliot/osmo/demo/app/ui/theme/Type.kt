package com.alliot.osmo.demo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DisplayFontFamily = FontFamily.SansSerif
private val BodyFontFamily = FontFamily.Default

private val DisplayTextStyle = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = (-0.5).sp,
)

private val BodyTextStyle = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.25.sp,
)

val AppTypography = Typography(
    displayLarge = DisplayTextStyle.copy(fontSize = 40.sp, lineHeight = 48.sp),
    displayMedium = DisplayTextStyle.copy(fontSize = 34.sp, lineHeight = 42.sp),
    displaySmall = DisplayTextStyle.copy(fontSize = 28.sp, lineHeight = 36.sp),
    bodyLarge = BodyTextStyle.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = BodyTextStyle.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = BodyTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp),
)
