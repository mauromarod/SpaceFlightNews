package com.mauromarod.spaceflightnews.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.mauromarod.spaceflightnews.core.designsystem.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val bebasNeue = GoogleFont("Bebas Neue")
private val spaceGrotesk = GoogleFont("Space Grotesk")
private val spaceMono = GoogleFont("Space Mono")

val BebasNeueFamily = FontFamily(
    Font(googleFont = bebasNeue, fontProvider = googleFontProvider, weight = FontWeight.Normal)
)

val SpaceGroteskFamily = FontFamily(
    Font(googleFont = spaceGrotesk, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = spaceGrotesk, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = spaceGrotesk, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = spaceGrotesk, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = spaceGrotesk, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

val SpaceMonoFamily = FontFamily(
    Font(googleFont = spaceMono, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = spaceMono, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

val VergeTypography = Typography(
    // Hero display — Bebas Neue at large scale (Manuka substitute)
    displayLarge = TextStyle(
        fontFamily = BebasNeueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 52.sp,
        letterSpacing = 1.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = BebasNeueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.8.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = BebasNeueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.5.sp,
    ),
    // Section + tile headlines — Space Grotesk bold
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.3.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // Eyebrow / capitalized thin label — signature Verge move
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Light,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.9.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
    ),
    // Reading body
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Mono uppercase labels (Space Mono — PolySans Mono substitute)
    labelLarge = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
)
