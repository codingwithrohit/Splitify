package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.Expense
import com.example.splitify.presentation.components.DashboardCard
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
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
        Text(
            text = "$totalExpenses ${if (totalExpenses == 1) "expense" else "expenses"} • ${currencyFormat.format(totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (recentExpenses.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No expenses yet. Tap to add!",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral600
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))

            recentExpenses.take(3).forEach { expense ->
                ExpensePreviewItem(expense = expense)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (totalExpenses > 3) {
                Text(
                    text = "Tap to view all ${totalExpenses} expenses →",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColors.Primary600
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getCategoryIcon(expense.category),
                contentDescription = expense.category.name,
                tint = PrimaryColors.Primary600,
                modifier = Modifier.size(20.dp)
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
            fontWeight = FontWeight.SemiBold,
            color = NeutralColors.Neutral700
        )
    }
}

// ExpenseCard.kt - Premium version
//@Composable
//fun PremiumExpenseCard(
//    expense: Expense,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        shape = CustomShapes.CardShape,
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Category icon with color
//            Surface(
//                modifier = Modifier.size(48.dp),
//                shape = CircleShape,
//                color = getCategoryColor(expense.category).copy(alpha = 0.2f)
//            ) {
//                Icon(
//                    imageVector = getCategoryIcon(expense.category),
//                    contentDescription = null,
//                    tint = getCategoryColor(expense.category),
//                    modifier = Modifier.padding(12.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            // Content
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = expense.description,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    color = NeutralColors.Neutral900
//                )
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Paid by ${expense.paidByName}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = NeutralColors.Neutral600
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "•",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = NeutralColors.Neutral400
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = expense.date.format(),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = NeutralColors.Neutral600
//                    )
//                }
//            }
//
//            // Amount
//            Text(
//                text = "₹${expense.amount}",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = PrimaryColors.Primary600
//            )
//        }
//    }
//}
//
//fun getCategoryColor(category: ExpenseCategory): Color {
//    return when (category) {
//        ExpenseCategory.FOOD -> CategoryColors.Food
//        ExpenseCategory.TRANSPORT -> CategoryColors.Transport
//        ExpenseCategory.ACCOMMODATION -> CategoryColors.Accommodation
//        ExpenseCategory.ENTERTAINMENT -> CategoryColors.Entertainment
//        ExpenseCategory.SHOPPING -> CategoryColors.Shopping
//        ExpenseCategory.OTHER -> CategoryColors.Other
//    }
//}
//
//fun getCategoryIcon(category: ExpenseCategory): ImageVector {
//    return when (category) {
//        ExpenseCategory.FOOD -> Icons.Default.Restaurant
//        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
//        ExpenseCategory.ACCOMMODATION -> Icons.Default.Hotel
//        ExpenseCategory.ENTERTAINMENT -> Icons.Default.TheaterComedy
//        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingCart
//        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
//    }
//}
