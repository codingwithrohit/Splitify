package com.example.splitify.domain.usecase.insights

import com.example.splitify.domain.model.TripInsights
import com.example.splitify.util.CurrencyUtils
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Generate a text summary of trip for sharing
 */
class GenerateTripSummaryUseCase @Inject constructor() {

    operator fun invoke(insights: TripInsights): String {
        return buildString {
            // Header
            appendLine("═══════════════════════════════════")
            appendLine("${insights.tripName.uppercase()} - SUMMARY")
            appendLine("═══════════════════════════════════")
            appendLine()

            // Date range
            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val dateRange = if (insights.endDate != null) {
                "${insights.startDate.format(dateFormatter)} - ${
                    insights.endDate.format(
                        dateFormatter
                    )
                }"
            } else {
                "Started ${insights.startDate.format(dateFormatter)}"
            }
            appendLine("📅 $dateRange (${insights.tripDurationDays} days)")
            appendLine()

            // Overview
            appendLine("💰 OVERVIEW")
            appendLine("─".repeat(35))
            appendLine("Total Expenses:     ${CurrencyUtils.format(insights.totalSpending)}")
            appendLine("Participants:       ${insights.totalMembers} members")
            appendLine("Transactions:       ${insights.totalExpenses} expenses")
            appendLine("Average per person: ${CurrencyUtils.format(insights.averagePerPerson)}")
            appendLine("Average per day:    ${CurrencyUtils.format(insights.totalSpending / insights.tripDurationDays)}")
            appendLine()

            //Who paid what
            if (insights.memberSpending.isNotEmpty()){
                appendLine("👥 WHO PAID WHAT?")
                appendLine("─".repeat(35))
                insights.memberSpending.forEach{ member ->
                    val bar = "█".repeat((member.percentage / 10).toInt())
                    val spaces = " ".repeat(10 - (member.percentage / 10).toInt())

                    appendLine(String.format("%-15s %8s (%4.1f%%) [%s%s]", member.member.displayName, CurrencyUtils.format(member.totalPaid), member.percentage, bar, spaces))

                }
                appendLine()
            }


            // Category breakdown
            if (insights.categorySpending.isNotEmpty()) {
                appendLine("📊 SPENDING BY CATEGORY")
                appendLine("─".repeat(35))
                insights.categorySpending.entries
                    .sortedByDescending { it.value }
                    .forEach { (category, amount) ->
                        val percentage = insights.categoryPercentages[category] ?: 0f
                        appendLine(
                            String.format(
                                "%-15s %8s (%4.1f%%)",
                                "${category.icon} ${category.displayName}",
                                CurrencyUtils.format(amount),
                                percentage
                            )
                        )
                    }
                appendLine()
            }

            // Expense type split
            if (insights.totalExpenses > 0) {
                appendLine("📝 EXPENSE TYPES")
                appendLine("─".repeat(35))
                appendLine("Group expenses:    ${insights.groupExpenseCount} (${CurrencyUtils.format(insights.groupExpensesTotal)})")
                appendLine("Personal expenses: ${insights.personalExpenseCount} (${CurrencyUtils.format(insights.personalExpensesTotal)})")
                appendLine()
            }

            // Footer
            appendLine("═══════════════════════════════════")
            appendLine("Generated via Splitify App")
            appendLine("${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}")


        }
    }
}