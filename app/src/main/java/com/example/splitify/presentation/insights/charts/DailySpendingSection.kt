package com.example.splitify.presentation.insights.charts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.TripInsights
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import com.example.splitify.util.CurrencyUtils
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailySpendingSection(insights: TripInsights) {
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
                text = "Daily Spending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Expenses by date",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (insights.dailySpending.isEmpty()) {
                Text(
                    text = "No daily data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralColors.Neutral600
                )
            } else {
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

                insights.dailySpending.entries.forEach { (date, amount) ->
                    val isHighest = date == insights.highestSpendingDay?.first

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralColors.Neutral700
                        )

                        Text(
                            text = CurrencyUtils.format(amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighest)
                                PrimaryColors.Primary600
                            else
                                NeutralColors.Neutral900
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (amount / (insights.highestSpendingDay?.second ?: 1.0)).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (isHighest)
                            PrimaryColors.Primary500
                        else
                            SecondaryColors.Secondary400,
                        trackColor = NeutralColors.Neutral200,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                insights.highestSpendingDay?.let { (date, amount) ->
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = NeutralColors.Neutral200
                    )

                    Surface(
                        shape = CustomShapes.CardShape,
                        color = PrimaryColors.Primary50
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Highest: ${date.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryColors.Primary700,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = CurrencyUtils.format(amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColors.Primary700
                            )
                        }
                    }
                }
            }
        }
    }
}