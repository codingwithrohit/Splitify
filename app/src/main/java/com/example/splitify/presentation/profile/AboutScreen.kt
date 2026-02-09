package com.example.splitify.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            SplitifyAppBar(
                title = "About Splitify",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColors.Neutral50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Logo and Version
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Icon placeholder - you can replace with your actual app icon
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Splitify Logo",
                    modifier = Modifier.size(80.dp),
                    tint = PrimaryColors.Primary600
                )

                Text(
                    text = "Splitify",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColors.Primary700
                )

                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralColors.Neutral600
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // About the App
            ProfileSection(title = "About") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Splitify is your ultimate expense tracking companion for group trips and shared expenses. Split bills effortlessly, track balances in real-time, and settle up with friends seamlessly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral700,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            // Features Section
            ProfileSection(title = "Key Features") {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureItem("📱", "Easy expense tracking")
                    FeatureItem("👥", "Group expense splitting")
                    FeatureItem("💰", "Real-time balance calculation")
                    //FeatureItem("🔄", "Multi-currency support")
                    FeatureItem("📊", "Detailed insights and analytics")
                    FeatureItem("🔒", "Secure and private")
                }
            }

            // Legal Links
            ProfileSection(title = "Legal") {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "How we handle your data",
                        onClick = { /* Open privacy policy URL */ }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = NeutralColors.Neutral200
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "Rules and guidelines",
                        onClick = { /* Open terms URL */ }
                    )
                }
            }

            // Copyright
            Text(
                text = "© 2026 Splitify. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FeatureItem(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralColors.Neutral700
        )
    }
}