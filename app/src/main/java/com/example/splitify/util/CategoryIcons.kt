package com.example.splitify.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.splitify.domain.model.Category

fun getCategoryIcon(category: Category): ImageVector{
    return when(category){
        Category.FOOD -> Icons.Default.Restaurant
        Category.TRANSPORT -> Icons.Default.DirectionsCar
        Category.ENTERTAINMENT -> Icons.Default.Movie
        Category.ACCOMMODATION -> Icons.Default.Hotel
        Category.SHOPPING -> Icons.Default.ShoppingCart
        Category.OTHER -> Icons.Default.MoreHoriz
    }
}