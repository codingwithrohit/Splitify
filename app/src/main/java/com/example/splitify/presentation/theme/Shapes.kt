package com.example.splitify.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Extra small - Chips, small badges
    extraSmall = RoundedCornerShape(4.dp),

    // Small - Small buttons, small cards
    small = RoundedCornerShape(8.dp),

    // Medium - Standard buttons, input fields, cards
    medium = RoundedCornerShape(12.dp),

    // Large - Large cards, bottom sheets
    large = RoundedCornerShape(16.dp),

    // Extra large - Full screen sheets, modals
    extraLarge = RoundedCornerShape(24.dp)
)

object CustomShapes {
    // Buttons
    val ButtonShape = RoundedCornerShape(12.dp)
    val ButtonLargeShape = RoundedCornerShape(16.dp)
    val ButtonPillShape = RoundedCornerShape(50) // Fully rounded

    // Cards
    val CardShape = RoundedCornerShape(16.dp)
    val CardElevatedShape = RoundedCornerShape(20.dp)

    // Input fields
    val TextFieldShape = RoundedCornerShape(12.dp)
    val TextFieldRoundedShape = RoundedCornerShape(24.dp)

    // Bottom sheets & modals
    val BottomSheetShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Dialogs
    val DialogShape = RoundedCornerShape(28.dp)

    // Category chips
    val ChipShape = RoundedCornerShape(8.dp)

    // Balance cards (extra rounded for premium feel)
    val BalanceCardShape = RoundedCornerShape(24.dp)
}