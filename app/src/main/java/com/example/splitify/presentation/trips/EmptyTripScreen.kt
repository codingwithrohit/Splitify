package com.example.splitify.presentation.trips

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun EmptyTripScreen(
    onCreateTripClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryColors.Primary50,
                            Color.White
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            PremiumTopBar(
                title = "My Trips",
                onLogoutClick = onLogoutClick
            )

            // Empty State Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Illustration
                AnimatedIllustration()

                Spacer(modifier = Modifier.height(32.dp))

                // Empty State Text
                Text(
                    text = "No Trips Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralColors.Neutral900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Create your first trip to start tracking\nexpenses with friends and family.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeutralColors.Neutral600,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Feature Highlights
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeatureItem(
                        icon = Icons.Default.Groups,
                        text = "Split expenses with unlimited members",
                        color = PrimaryColors.Primary500
                    )
                    FeatureItem(
                        icon = Icons.Default.Calculate,
                        text = "Smart balance calculation & settlement",
                        color = SecondaryColors.Secondary500
                    )
                    FeatureItem(
                        icon = Icons.Default.Sync,
                        text = "Real-time sync across all devices",
                        color = AccentColors.Accent500
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Create Trip Button
                Button(
                    onClick = onCreateTripClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CustomShapes.ButtonLargeShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColors.Primary600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Your First Trip",
                        style = CustomTextStyles.ButtonLarge
                    )
                }
            }
        }

    }
}

@Composable
private fun PremiumTopBar(
    title: String,
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = PrimaryColors.Primary100
                ) {
                    Icon(
                        imageVector = Icons.Default.Luggage,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = PrimaryColors.Primary600
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralColors.Neutral900
                )
            }

            TextButton(onClick = onLogoutClick) {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeutralColors.Neutral600
                )
            }
        }
    }
}

@Composable
private fun AnimatedIllustration() {
    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .offset(y = offsetY.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            color = PrimaryColors.Primary100.copy(alpha = 0.5f)
        ) {}

        // Icon
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PrimaryColors.Primary500,
            shadowElevation = 8.dp
        ) {
            Icon(
                imageVector = Icons.Default.Luggage,
                contentDescription = null,
                modifier = Modifier.padding(24.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = color
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralColors.Neutral700
        )
    }
}