package com.example.splitify.presentation.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.data.local.SessionManager
import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.Trip
import com.example.splitify.presentation.components.AnimatedCard
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.components.LoadingScreen
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors
import com.example.splitify.util.PullToRefreshBox
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TripsScreen(
    modifier: Modifier,
    onCreateTripClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onJoinTripClick: () -> Unit,
    onLogOut: () -> Unit,
    viewModel: TripsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId by viewModel.userId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            onLogOut()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {

            TripsUiState.InitialLoading -> {
                LoadingScreen("Preparing your trips…")
            }

            is TripsUiState.Empty -> {
                EmptyTripScreen(
                    onCreateTripClick = onCreateTripClick,
                    onLogoutClick = { viewModel.logout() }
                )
            }

            is TripsUiState.Content -> {
                val state = uiState as TripsUiState.Content

                PullToRefreshBox(
                    isRefreshing = state.isSyncing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    TripsList(
                        trips = state.trips,
                        onTripClick = onTripClick,
                        onDeleteTrip = viewModel::deleteTrip,
                        currentUserId = currentUserId
                    )
                }
            }

            is TripsUiState.Error -> {
                ErrorStateWithRetry(
                    message = (uiState as TripsUiState.Error).message,
                    onRetry = viewModel::refresh
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
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = trips,
            key = { trip -> trip.id }
        ){ trip ->
            TripCardWithDeleteConfirmation(
                trip = trip,
                onClick = { onTripClick(trip.id) },
                onDelete = { onDeleteTrip(trip.id) },
                currentUserId = currentUserId,
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun formatDateRange(startDate: LocalDate, endDate: LocalDate?): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd")
    val start = startDate.format(formatter)
    return if (endDate != null) {
        val end = endDate.format(formatter)
        "$start → $end"
    } else {
        start
    }
}

@Composable
fun TripCardWithDeleteConfirmation(
    trip: Trip,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    AnimatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (trip.description != null && trip.description.isNotBlank()) {
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PrimaryColors.Primary600
                        )
                        Text(
                            text = formatDateRange(trip.startDate, trip.endDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralColors.Neutral700
                        )
                    }
                }

                Surface(
                    shape = CustomShapes.ChipShape,
                    color = PrimaryColors.Primary50
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryColors.Primary700
                        )
                        Text(
                            text = trip.inviteCode,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColors.Primary700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if(trip.createdBy == currentUserId){
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete trip",
                        tint = SemanticColors.Error
                    )
                }
            }

        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = SemanticColors.Error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Delete Trip?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete '${trip.name}'? This action cannot be undone.",
                    color = NeutralColors.Neutral600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticColors.Error
                    ),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }
}