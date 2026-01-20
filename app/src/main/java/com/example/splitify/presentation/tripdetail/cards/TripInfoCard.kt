package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.Trip
import com.example.splitify.presentation.components.InfoChip
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.CustomTextStyles
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import com.example.splitify.util.formatToIndianCurrency
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalLayoutApi::class)

@Composable
fun TripInfoCard(
    trip: Trip,
    memberCount: Int,
    totalAmount: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = CustomShapes.CardElevatedShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryColors.Primary400,
                                PrimaryColors.Primary600
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${memberCount} members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Total expense with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = SecondaryColors.Secondary500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatToIndianCurrency(totalAmount),
                        style = CustomTextStyles.CurrencyMedium,
                        color = NeutralColors.Neutral900
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date range
                Text(
                    text = "${trip.startDate} - ${trip.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralColors.Neutral600
                )
            }
        }
    }
}

//fun formatDateRange(
//    startDate: LocalDate,
//    endDate: LocalDate?
//): String {
//    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
//    val start = startDate.format(formatter)
//
//    return if (endDate != null && endDate != startDate) {
//        "${startDate.format(formatter)} - ${endDate.format(formatter)}"
//    } else {
//        start
//    }
//}

fun formatSmartDateRange(
    startDate: LocalDate,
    endDate: LocalDate?
): String {
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    val fullFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    if (endDate == null) {
        return startDate.format(fullFormatter)
    }

    return if (
        startDate.month == endDate.month &&
        startDate.year == endDate.year
    ) {
        "${startDate.format(dayFormatter)} - ${endDate.format(fullFormatter)}"
    } else {
        "${startDate.format(fullFormatter)} - ${endDate.format(fullFormatter)}"
    }
}



//private fun formatDateRange(startDate: Long, endDate: Long?): String {
//    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
//    val start = dateFormat.format(Date(startDate))
//
//    return if (endDate != null && endDate != startDate) {
//        val end = dateFormat.format(Date(endDate))
//        "$start - $end"
//    } else {
//        start
//    }
//}