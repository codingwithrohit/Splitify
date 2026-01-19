package com.example.splitify.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category (
    val displayName: String,
    val icon: String,
){
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚗"),
    ACCOMMODATION("Accommodation", "🏨"),
    ENTERTAINMENT("Entertainment", "🎬"),
    SHOPPING("Shopping", "🛍️"),
    OTHER("Other", "📌");

    companion object{
        fun fromString(value: String): Category{
            return Category.entries.find {it.name == value} ?: OTHER
        }
    }
}
