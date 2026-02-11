package com.example.splitify.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.NotificationType
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors

/**
 * Custom Snackbar that matches your app's Toast design
 * Use this in MainScreen's SnackbarHost
 */
@Composable
fun CustomNotificationSnackbar(
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. Identify type based on message keywords
    val notificationType = when {
        message.contains("Success", ignoreCase = true) ||
                message.contains("Created", ignoreCase = true) ||
                message.contains("Updated", ignoreCase = true) ||
                message.contains("Added", ignoreCase = true) -> NotificationType.SUCCESS

        message.contains("Error", ignoreCase = true) ||
                message.contains("Failed", ignoreCase = true) -> NotificationType.ERROR

        message.contains("Warning", ignoreCase = true) ||
                message.contains("Deleted", ignoreCase = true) -> NotificationType.WARNING

        else -> NotificationType.INFO
    }

    // 2. Map colors directly to your SemanticColors from Color.kt
    val (icon, backgroundColor, contentColor) = when (notificationType) {
        NotificationType.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            SemanticColors.Success, // Using 0xFF10B981
            Color.White
        )
        NotificationType.ERROR -> Triple(
            Icons.Default.Error,
            SemanticColors.Error,   // Using 0xFFEF4444
            Color.White
        )
        NotificationType.WARNING -> Triple(
            Icons.Default.Warning,
            SemanticColors.Warning, // Using 0xFFF59E0B
            Color.White
        )
        NotificationType.INFO -> Triple(
            Icons.Default.Info,
            PrimaryColors.Primary600, // Matching your FAB/Main brand
            Color.White
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Padding from screen edges
        shape = CustomShapes.CardShape, // Using your defined 16.dp rounded corners
        color = backgroundColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with a subtle "inner glow" feel by using white with slight alpha
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = contentColor
                )
            }

            if (actionLabel != null) {
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}