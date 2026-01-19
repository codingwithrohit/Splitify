package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.components.DashboardCard
import com.example.splitify.presentation.theme.NeutralColors

@Composable
fun InsightsPreviewCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        title = "Insights",
        icon = Icons.Default.Insights,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = "View spending analytics and trends",
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralColors.Neutral600
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            InsightItem("📊 Category breakdown")
            InsightItem("📈 Spending timeline")
            InsightItem("👥 Top spenders")
        }
    }
}

@Composable
private fun InsightItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )
}