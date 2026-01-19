package com.example.splitify.util

import java.text.NumberFormat
import java.util.Locale

fun formatToIndianCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 0
    return formatter.format(amount)
}
