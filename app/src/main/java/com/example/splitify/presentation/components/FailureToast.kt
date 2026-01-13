package com.example.splitify.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FailureToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Long = 3000 // Errors usually benefit from slightly longer display time
) {
    // Auto-dismiss logic
    LaunchedEffect(visible) {
        if (visible) {
            delay(durationMillis)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error, // Changed to error color
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error, // Changed to error icon
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}