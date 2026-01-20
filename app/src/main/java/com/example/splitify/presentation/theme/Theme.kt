package com.example.splitify.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val LightColors = lightColorScheme(
    // Primary colors
    primary = PrimaryColors.Primary500,
    onPrimary = Color.White,
    primaryContainer = PrimaryColors.Primary100,
    onPrimaryContainer = PrimaryColors.Primary900,

    // Secondary colors
    secondary = SecondaryColors.Secondary500,
    onSecondary = Color.White,
    secondaryContainer = SecondaryColors.Secondary100,
    onSecondaryContainer = SecondaryColors.Secondary900,

    // Tertiary colors
    tertiary = AccentColors.Accent500,
    onTertiary = Color.White,
    tertiaryContainer = AccentColors.Accent100,
    onTertiaryContainer = AccentColors.Accent900,

    // Background colors
    background = Color.White,
    onBackground = NeutralColors.Neutral900,

    // Surface colors
    surface = NeutralColors.Neutral50,
    onSurface = NeutralColors.Neutral900,
    surfaceVariant = NeutralColors.Neutral100,
    onSurfaceVariant = NeutralColors.Neutral700,

    // Error colors
    error = SemanticColors.Error,
    onError = Color.White,
    errorContainer = SemanticColors.ErrorLight,
    onErrorContainer = SemanticColors.ErrorDark,

    // Outline colors
    outline = NeutralColors.Neutral300,
    outlineVariant = NeutralColors.Neutral200,

    // Other
    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = NeutralColors.Neutral800,
    inverseOnSurface = NeutralColors.Neutral50,
    inversePrimary = PrimaryColors.Primary400,

    // Surface tints
    surfaceTint = PrimaryColors.Primary500,
)

private val DarkColors = darkColorScheme(
    // Primary colors
    primary = PrimaryColors.Primary400,
    onPrimary = PrimaryColors.Primary900,
    primaryContainer = PrimaryColors.Primary700,
    onPrimaryContainer = PrimaryColors.Primary100,

    // Secondary colors
    secondary = SecondaryColors.Secondary400,
    onSecondary = SecondaryColors.Secondary900,
    secondaryContainer = SecondaryColors.Secondary700,
    onSecondaryContainer = SecondaryColors.Secondary100,

    // Tertiary colors
    tertiary = AccentColors.Accent400,
    onTertiary = AccentColors.Accent900,
    tertiaryContainer = AccentColors.Accent700,
    onTertiaryContainer = AccentColors.Accent100,

    // Background colors
    background = NeutralColors.Neutral900,
    onBackground = NeutralColors.Neutral50,

    // Surface colors
    surface = NeutralColors.Neutral800,
    onSurface = NeutralColors.Neutral50,
    surfaceVariant = NeutralColors.Neutral700,
    onSurfaceVariant = NeutralColors.Neutral300,

    // Error colors
    error = SemanticColors.Error,
    onError = SemanticColors.ErrorDark,
    errorContainer = SemanticColors.ErrorDark,
    onErrorContainer = SemanticColors.ErrorLight,

    // Outline colors
    outline = NeutralColors.Neutral600,
    outlineVariant = NeutralColors.Neutral700,

    // Other
    scrim = Color.Black.copy(alpha = 0.64f),
    inverseSurface = NeutralColors.Neutral100,
    inverseOnSurface = NeutralColors.Neutral900,
    inversePrimary = PrimaryColors.Primary600,

    // Surface tints
    surfaceTint = PrimaryColors.Primary400,
)


@Composable
fun SplitifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    // Update system bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar color (transparent for gradient backgrounds)
            window.statusBarColor = Color.Transparent.toArgb()

            // Set navigation bar color
            window.navigationBarColor = if (darkTheme) {
                NeutralColors.Neutral900.toArgb()
            } else {
                Color.White.toArgb()
            }

            // Set system UI icons color
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        //shapes = Shapes
    )
}

@Composable
fun SplitifyLightTheme(content: @Composable () -> Unit) {
    SplitifyTheme(darkTheme = false, content = content)
}

@Composable
fun SplitifyDarkTheme(content: @Composable () -> Unit) {
    SplitifyTheme(darkTheme = true, content = content)
}