package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.components.DashboardCard

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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "📊 Category breakdown\n📈 Spending timeline\n👥 Top spenders",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}