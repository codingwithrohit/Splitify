package com.example.splitify.presentation.theme

import androidx.compose.ui.graphics.Color

object PrimaryColors {
    val Primary50 = Color(0xFFF0F4FF)
    val Primary100 = Color(0xFFE0E7FF)
    val Primary200 = Color(0xFFC7D2FE)
    val Primary300 = Color(0xFFA5B4FC)
    val Primary400 = Color(0xFF818CF8)
    val Primary500 = Color(0xFF6366F1) // Main brand color
    val Primary600 = Color(0xFF4F46E5)
    val Primary700 = Color(0xFF4338CA)
    val Primary800 = Color(0xFF3730A3)
    val Primary900 = Color(0xFF312E81)
}


object SecondaryColors {
    val Secondary50 = Color(0xFFECFDF5)
    val Secondary100 = Color(0xFFD1FAE5)
    val Secondary200 = Color(0xFFA7F3D0)
    val Secondary300 = Color(0xFF6EE7B7)
    val Secondary400 = Color(0xFF34D399)
    val Secondary500 = Color(0xFF10B981) // Main success color
    val Secondary600 = Color(0xFF059669)
    val Secondary700 = Color(0xFF047857)
    val Secondary800 = Color(0xFF065F46)
    val Secondary900 = Color(0xFF064E3B)
}

// ACCENT COLORS - Energy & Attention
// Coral-Orange for CTAs and highlights
object AccentColors {
    val Accent50 = Color(0xFFFFF7ED)
    val Accent100 = Color(0xFFFFEDD5)
    val Accent200 = Color(0xFFFED7AA)
    val Accent300 = Color(0xFFFDBA74)
    val Accent400 = Color(0xFFFB923C)
    val Accent500 = Color(0xFFF97316) // Main accent
    val Accent600 = Color(0xFFEA580C)
    val Accent700 = Color(0xFFC2410C)
    val Accent800 = Color(0xFF9A3412)
    val Accent900 = Color(0xFF7C2D12)
}

// NEUTRAL COLORS - UI Elements
// Slate gray for text and backgrounds
object NeutralColors {
    val Neutral50 = Color(0xFFF8FAFC)
    val Neutral100 = Color(0xFFF1F5F9)
    val Neutral200 = Color(0xFFE2E8F0)
    val Neutral300 = Color(0xFFCBD5E1)
    val Neutral400 = Color(0xFF94A3B8)
    val Neutral500 = Color(0xFF64748B)
    val Neutral600 = Color(0xFF475569)
    val Neutral700 = Color(0xFF334155)
    val Neutral800 = Color(0xFF1E293B)
    val Neutral900 = Color(0xFF0F172A)
}

// SEMANTIC COLORS - Status Indicators
object SemanticColors {
    // Error - Red
    val Error = Color(0xFFEF4444)
    val ErrorLight = Color(0xFFFEE2E2)
    val ErrorDark = Color(0xFFDC2626)

    // Warning - Amber
    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFEF3C7)
    val WarningDark = Color(0xFFD97706)

    // Success - Green
    val Success = Color(0xFF10B981)
    val SuccessLight = Color(0xFFD1FAE5)
    val SuccessDark = Color(0xFF059669)

    // Info - Blue
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFFDBEAFE)
    val InfoDark = Color(0xFF2563EB)
}

// GRADIENT COLORS - Premium Backgrounds
object GradientColors {
    // Login/Signup Screen Gradient
    val PrimaryGradientStart = Color(0xFF667EEA) // Purple
    val PrimaryGradientEnd = Color(0xFF764BA2)   // Deep Purple

    // Success Gradient (for positive actions)
    val SuccessGradientStart = Color(0xFF56CCF2) // Light Blue
    val SuccessGradientEnd = Color(0xFF2F80ED)   // Blue

    // Accent Gradient (for CTAs)
    val AccentGradientStart = Color(0xFFF093FB)  // Pink
    val AccentGradientEnd = Color(0xFFF5576C)    // Coral

    // Warm Gradient (for cards/highlights)
    val WarmGradientStart = Color(0xFFFDC830)    // Yellow
    val WarmGradientEnd = Color(0xFFF37335)      // Orange

    // Cool Gradient (for balance/analytics)
    val CoolGradientStart = Color(0xFF4FACFE)    // Light Blue
    val CoolGradientEnd = Color(0xFF00F2FE)      // Cyan
}

// CATEGORY COLORS - Expense Categories
// Vibrant, easily distinguishable colors
object CategoryColors {
    val Food = Color(0xFFFF6B6B)        // Coral Red
    val Transport = Color(0xFF4ECDC4)   // Turquoise
    val Accommodation = Color(0xFF95E1D3) // Mint Green
    val Entertainment = Color(0xFFFFBE0B) // Yellow
    val Shopping = Color(0xFFE056FD)    // Purple
    val Other = Color(0xFF94A3B8)       // Neutral Gray
}

data class LightColorScheme(
    val primary: Color = PrimaryColors.Primary500,
    val onPrimary: Color = Color.White,
    val primaryContainer: Color = PrimaryColors.Primary100,
    val onPrimaryContainer: Color = PrimaryColors.Primary900,

    val secondary: Color = SecondaryColors.Secondary500,
    val onSecondary: Color = Color.White,
    val secondaryContainer: Color = SecondaryColors.Secondary100,
    val onSecondaryContainer: Color = SecondaryColors.Secondary900,

    val tertiary: Color = AccentColors.Accent500,
    val onTertiary: Color = Color.White,
    val tertiaryContainer: Color = AccentColors.Accent100,
    val onTertiaryContainer: Color = AccentColors.Accent900,

    val background: Color = Color.White,
    val onBackground: Color = NeutralColors.Neutral900,
    val surface: Color = NeutralColors.Neutral50,
    val onSurface: Color = NeutralColors.Neutral900,
    val surfaceVariant: Color = NeutralColors.Neutral100,
    val onSurfaceVariant: Color = NeutralColors.Neutral700,

    val error: Color = SemanticColors.Error,
    val onError: Color = Color.White,
    val errorContainer: Color = SemanticColors.ErrorLight,
    val onErrorContainer: Color = SemanticColors.ErrorDark,

    val outline: Color = NeutralColors.Neutral300,
    val outlineVariant: Color = NeutralColors.Neutral200,
    val scrim: Color = Color.Black.copy(alpha = 0.32f),
)


// DARK THEME COLORS
data class DarkColorScheme(
    val primary: Color = PrimaryColors.Primary400,
    val onPrimary: Color = PrimaryColors.Primary900,
    val primaryContainer: Color = PrimaryColors.Primary700,
    val onPrimaryContainer: Color = PrimaryColors.Primary100,

    val secondary: Color = SecondaryColors.Secondary400,
    val onSecondary: Color = SecondaryColors.Secondary900,
    val secondaryContainer: Color = SecondaryColors.Secondary700,
    val onSecondaryContainer: Color = SecondaryColors.Secondary100,

    val tertiary: Color = AccentColors.Accent400,
    val onTertiary: Color = AccentColors.Accent900,
    val tertiaryContainer: Color = AccentColors.Accent700,
    val onTertiaryContainer: Color = AccentColors.Accent100,

    val background: Color = NeutralColors.Neutral900,
    val onBackground: Color = NeutralColors.Neutral50,
    val surface: Color = NeutralColors.Neutral800,
    val onSurface: Color = NeutralColors.Neutral50,
    val surfaceVariant: Color = NeutralColors.Neutral700,
    val onSurfaceVariant: Color = NeutralColors.Neutral300,

    val error: Color = SemanticColors.Error,
    val onError: Color = SemanticColors.ErrorDark,
    val errorContainer: Color = SemanticColors.ErrorDark,
    val onErrorContainer: Color = SemanticColors.ErrorLight,

    val outline: Color = NeutralColors.Neutral600,
    val outlineVariant: Color = NeutralColors.Neutral700,
    val scrim: Color = Color.Black.copy(alpha = 0.64f),
)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val md_theme_light_warningContainer = Color(0xFFFFF4E5)
val md_theme_light_onWarningContainer = Color(0xFF8B5A00)

val md_theme_dark_warningContainer = Color(0xFF4D3800)
val md_theme_dark_onWarningContainer = Color(0xFFFFDDB3)