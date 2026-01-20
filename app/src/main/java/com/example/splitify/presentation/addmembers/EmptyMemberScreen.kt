package com.example.splitify.presentation.addmembers

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitify.presentation.theme.*

@Composable
fun EmptyMemberScreen(
    onAddMembers: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box( modifier = modifier .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf( Color.White,
                    SecondaryColors.Secondary50.copy(alpha = 0.5f)
                )
            )
        ), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {

            AnimatedMemberIllustration()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "No Members Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Add people to this trip to start splitting\nexpenses and tracking balances together.",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral600,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onAddMembers,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                shape = CustomShapes.ButtonLargeShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryColors.Secondary600
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Members",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable private fun AnimatedMemberIllustration() {

    val infiniteTransition = rememberInfiniteTransition(label = "memberFloat")

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(160.dp)
            .offset(y = floatAnim.dp)
            .scale(scaleAnim)
    ) {
        // Decorative soft circles
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = SecondaryColors.Secondary100.copy(alpha = 0.4f)
        ) {}

        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = SecondaryColors.Secondary500,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
    }
}