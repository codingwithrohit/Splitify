package com.example.splitify.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    private val indianFormatter: NumberFormat by lazy {
        NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
            isGroupingUsed = true
        }
    }

    fun format(amount: Double): String {
        return "₹${indianFormatter.format(amount)}"
    }

    fun format(amount: Long): String {
        return "₹${indianFormatter.format(amount)}"
    }

    fun format(amount: String): String {
        val value = amount.toDoubleOrNull() ?: return "₹0"
        return format(value)
    }
}