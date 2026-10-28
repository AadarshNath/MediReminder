package com.medassist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MedTrackerLightColorScheme = lightColorScheme(
    primary = MedicalBlue,
    onPrimary = Color.White,
    primaryContainer = MedicalBlueLight,
    onPrimaryContainer = MedicalBlueDark,

    secondary = HealthTeal,
    onSecondary = Color.White,
    secondaryContainer = HealthTealLight,
    onSecondaryContainer = HealthTealDark,

    tertiary = AccentMint,
    onTertiary = Color.White,
    tertiaryContainer = AccentMintLight,
    onTertiaryContainer = Color(0xFF00201B),

    background = BackgroundWhite,
    onBackground = TextPrimaryDark,

    surface = SurfaceWhite,
    onSurface = TextPrimaryDark,

    surfaceVariant = SurfaceVariantClean,
    onSurfaceVariant = TextSecondaryMuted,

    outline = OutlineBorder,
    outlineVariant = DividerColor,

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MedAssistTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MedTrackerLightColorScheme,
        typography = MedAssistTypography,
        shapes = MedAssistShapes,
        content = content,
    )
}
