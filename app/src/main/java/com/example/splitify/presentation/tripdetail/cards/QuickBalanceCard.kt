package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.components.DashboardCard
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.SemanticColors
import com.example.splitify.presentation.theme.SecondaryColors
import java.text.NumberFormat
import java.util.*

@Composable
fun QuickBalanceCard(
    youOwe: Double,
    youAreOwed: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    DashboardCard(
        title = "Balances",
        icon = Icons.Default.AccountBalanceWallet,
        onClick = onClick,
        modifier = modifier
    ) {
        if (youOwe == 0.0 && youAreOwed == 0.0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SecondaryColors.Secondary50,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SecondaryColors.Secondary600,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "All settled!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryColors.Secondary700
                )
            }
        } else {
            if (youOwe > 0) {
                BalanceRow(
                    label = "You owe:",
                    amount = currencyFormat.format(youOwe),
                    color = SemanticColors.Error
                )
            }

            if (youOwe > 0 && youAreOwed > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (youAreOwed > 0) {
                BalanceRow(
                    label = "You're owed:",
                    amount = currencyFormat.format(youAreOwed),
                    color = SecondaryColors.Secondary600
                )
            }
        }
    }
}

@Composable
private fun BalanceRow(
    label: String,
    amount: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralColors.Neutral700
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}