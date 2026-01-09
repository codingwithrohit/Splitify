package com.example.splitify.presentation.insights.charts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.TripInsights
import com.example.splitify.util.CurrencyUtils
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@Composable
fun CategoryBreakdownSection(insights: TripInsights) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (insights.categorySpending.isEmpty()) {
                Text(
                    text = "No category data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Category list with bars
                insights.categorySpending.entries
                    .sortedByDescending { it.value }
                    .forEach { (category, amount) ->
                        CategoryItem(
                            category = category,
                            amount = amount,
                            percentage = insights.categoryPercentages[category] ?: 0f,
                            totalSpending = insights.totalSpending
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    amount: Double,
    percentage: Float,
    totalSpending: Double
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = (amount / totalSpending).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = getCategoryColor(category),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.FOOD -> Color(0xFFFF6B6B)
        Category.TRANSPORT -> Color(0xFF4ECDC4)
        Category.ACCOMMODATION -> Color(0xFFFFE66D)
        Category.ENTERTAINMENT -> Color(0xFF95E1D3)
        Category.SHOPPING -> Color(0xFFC7CEEA)
        Category.OTHER -> Color(0xFFB4A7D6)
    }
}