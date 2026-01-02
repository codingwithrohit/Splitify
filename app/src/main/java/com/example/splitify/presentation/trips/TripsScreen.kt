package com.example.splitify.presentation.trips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.Trip
import com.example.splitify.presentation.components.EmptyTripsState
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.components.LoadingScreen
import com.example.splitify.util.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    onCreateTripClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onLogOut: () -> Unit,
    viewModel: TripsViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

//    LaunchedEffect(uiState) {
//        isRefreshing = false
//    }
    LaunchedEffect(uiState) {
        if (uiState is TripsUiState.Success || uiState is TripsUiState.Error) {
            isRefreshing = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips") },
                actions = {
                    TextButton(onClick = onLogOut)  {
                    Text("Logout", color = MaterialTheme.colorScheme.onPrimary)
                } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTripClick
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Trip")
            }
        }
    ){paddingValues ->

        when(uiState){
            is TripsUiState.Loading -> {
                if(!isRefreshing){
                    LoadingScreen(
                        message = "Loading your trips...",
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    )
                }
            }
            is TripsUiState.Success -> {
                val trips = (uiState as TripsUiState.Success).trips
                if(trips.isEmpty()){
                    EmptyTripsState(
                        onCreateTripClick,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    )
                }
                else{
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.refresh()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TripsList(
                            trips = trips,
                            onTripClick = onTripClick,
                            onDeleteTrip = { tripId -> viewModel.deleteTrip(tripId) },
                            modifier = Modifier.fillMaxSize().padding(paddingValues)
                        )
                    }
                }
            }
            is TripsUiState.Error -> {
                ErrorStateWithRetry(
                    message = (uiState as TripsUiState.Error).message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
        }

    }
}

@Composable
fun TripsList(
    trips: List<Trip>,
    onTripClick: (String) -> Unit,
    onDeleteTrip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items  = trips,
            key = { trip -> trip.id }
        ){ trip ->
            TripCard(
                trip =  trip,
                onClick = {onTripClick(trip.id)},
                onDelete = { onDeleteTrip(trip.id) }
            )
        }
    }

}

@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (trip.description != null) {
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formatDateRange(trip.startDate.toString(), trip.endDate?.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Code: ${trip.inviteCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete trip",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

    }

}
private fun formatDateRange(startDate: String, endDate: String?): String {
    return if (endDate != null) {
        "$startDate → $endDate"
    } else {
        startDate
    }
}






