package com.example.splitify.presentation.tripdetail.cards

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.components.DashboardCard
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.util.getCategoryIcon

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
            InsightItem(Icons.Default.PieChart, "Category breakdown")
            InsightItem(Icons.Default.ShowChart,"Spending timeline")
            InsightItem(Icons.Default.People,"Top spenders")
        }
    }
}

@Composable
private fun InsightItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.toString(),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}