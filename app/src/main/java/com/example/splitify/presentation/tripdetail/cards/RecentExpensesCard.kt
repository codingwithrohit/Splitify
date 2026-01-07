package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.Expense
import com.example.splitify.presentation.components.DashboardCard
import com.example.splitify.util.getCategoryIcon
import java.text.NumberFormat
import java.util.*

@Composable
fun RecentExpensesCard(
    totalExpenses: Int,
    totalAmount: Double,
    recentExpenses: List<Expense>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    DashboardCard(
        title = "Expenses",
        icon = Icons.Default.Receipt,
        onClick = onClick,
        modifier = modifier
    ) {
        // Summary
        Text(
            text = "$totalExpenses expenses • ${currencyFormat.format(totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (recentExpenses.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No expenses yet. Tap to add!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))

            // Show last 3 expenses
            recentExpenses.take(3).forEach { expense ->
                ExpensePreviewItem(expense = expense)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (totalExpenses > 3) {
                Text(
                    text = "Tap to view all ${totalExpenses} expenses →",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ExpensePreviewItem(expense: Expense) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(expense.category),
                contentDescription = expense.category.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = currencyFormat.format(expense.amount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}