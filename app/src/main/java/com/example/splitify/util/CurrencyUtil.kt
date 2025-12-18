package com.example.splitify.util

object CurrencyUtils {
    fun format(amount: Double): String {
        return "₹${"%.2f".format(amount)}"
    }
}