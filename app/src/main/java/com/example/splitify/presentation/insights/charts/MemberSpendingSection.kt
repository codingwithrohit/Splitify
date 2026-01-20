package com.example.splitify.presentation.insights.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.MemberSpending
import com.example.splitify.domain.model.TripInsights
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import com.example.splitify.util.CurrencyUtils

@Composable
fun MemberSpendingSection(insights: TripInsights) {
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
                text = "Who Paid What?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total contributions by each member",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (insights.memberSpending.isEmpty()) {
                Text(
                    text = "No member data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralColors.Neutral600
                )
            } else {
                insights.memberSpending.forEachIndexed { index, memberSpending ->
                    MemberSpendingItem(
                        memberSpending = memberSpending,
                        maxSpending = insights.memberSpending.firstOrNull()?.totalPaid ?: 1.0,
                        isTopSpender = index == 0
                    )
                    if (index < insights.memberSpending.size - 1) {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberSpendingItem(
    memberSpending: MemberSpending,
    maxSpending: Double,
    isTopSpender: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (isTopSpender)
                        PrimaryColors.Primary100
                    else
                        SecondaryColors.Secondary100
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = memberSpending.member.displayName
                                .firstOrNull()
                                ?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isTopSpender)
                                PrimaryColors.Primary700
                            else
                                SecondaryColors.Secondary700,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = memberSpending.member.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralColors.Neutral900
                        )
                        if (isTopSpender) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Top Spender",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                    Text(
                        text = "${memberSpending.expenseCount} expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralColors.Neutral600
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(memberSpending.totalPaid),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralColors.Neutral900
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isTopSpender)
                        PrimaryColors.Primary100
                    else
                        SecondaryColors.Secondary100
                ) {
                    Text(
                        text = String.format("%.1f%%", memberSpending.percentage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTopSpender)
                            PrimaryColors.Primary700
                        else
                            SecondaryColors.Secondary700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(NeutralColors.Neutral200)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((memberSpending.totalPaid / maxSpending).toFloat())
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isTopSpender)
                            PrimaryColors.Primary500
                        else
                            SecondaryColors.Secondary500
                    )
            )
        }
    }
}