package com.example.splitify.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

//@Composable
//fun AnimatedCard(
//    onClick: (() -> Unit)? = null,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val isPressed by interactionSource.collectIsPressedAsState()
//
//    val scale by animateFloatAsState(
//        targetValue = if (isPressed) 0.97f else 1f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "scale"
//    )
//
//    Card(
//        modifier = modifier
//            .scale(scale)
//            .then(
//                if (onClick != null) {
//                    Modifier.clickable(
//                        interactionSource = interactionSource,
//                        indication = null,
//                        enabled = enabled,
//                        onClick = onClick
//                    )
//                } else Modifier
//            ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (isPressed) 1.dp else 4.dp
//        )
//    ) {
//        content()
//    }
//}

@Composable
fun AnimatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Animate scale when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val success = tryAwaitRelease()
                        isPressed = false
                        if (success) {
                            onClick()
                        }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        content = content
    )
}


@Composable
fun AnimatedCardWithElevation(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    defaultElevation: androidx.compose.ui.unit.Dp = 2.dp,
    pressedElevation: androidx.compose.ui.unit.Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "card_scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) pressedElevation.value else defaultElevation.value,
        animationSpec = tween(durationMillis = 100),
        label = "card_elevation"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        content = content
    )
}