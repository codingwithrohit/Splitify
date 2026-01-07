package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.Trip
import com.example.splitify.presentation.components.InfoChip
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripInfoCard(
    trip: Trip,
    memberCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = trip.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2
            )

            if (trip.description?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.CalendarToday,
                    text = formatSmartDateRange(trip.startDate,trip.endDate )
                )

                InfoChip(
                    icon = Icons.Default.People,
                    text = "$memberCount members"
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