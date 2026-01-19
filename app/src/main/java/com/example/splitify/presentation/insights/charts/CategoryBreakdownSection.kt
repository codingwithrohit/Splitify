package com.example.splitify.presentation.insights.charts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.TripInsights
import com.example.splitify.presentation.theme.CategoryColors
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.util.CurrencyUtils

@Composable
fun CategoryBreakdownSection(insights: TripInsights) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (insights.categorySpending.isEmpty()) {
                Text(
                    text = "No category data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralColors.Neutral600
                )
            } else {
                insights.categorySpending.entries
                    .sortedByDescending { it.value }
                    .forEach { (category, amount) ->
                        CategoryItem(
                            category = category,
                            amount = amount,
                            percentage = insights.categoryPercentages[category] ?: 0f,
                            totalSpending = insights.totalSpending
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
    val categoryColor = when (category) {
        Category.FOOD -> CategoryColors.Food
        Category.TRANSPORT -> CategoryColors.Transport
        Category.ACCOMMODATION -> CategoryColors.Accommodation
        Category.ENTERTAINMENT -> CategoryColors.Entertainment
        Category.SHOPPING -> CategoryColors.Shopping
        Category.OTHER -> CategoryColors.Other
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeutralColors.Neutral900
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeutralColors.Neutral900
                )
                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (amount / totalSpending).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = categoryColor,
            trackColor = NeutralColors.Neutral200,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}