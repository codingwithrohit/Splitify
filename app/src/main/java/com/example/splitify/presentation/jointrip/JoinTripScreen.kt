package com.example.splitify.presentation.jointrip

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.Trip
import com.example.splitify.presentation.components.LoadingScreen
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun JoinTripScreen(
    onBackClick: () -> Unit,
    onTripJoined: (String) -> Unit,
    viewModel: JoinTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inviteCode by viewModel.inviteCode.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is JoinTripUiState.Success) {
            val tripId = (uiState as JoinTripUiState.Success).tripId
            onTripJoined(tripId)
        }
    }

    Scaffold(
        topBar = {
            SplitifyAppBar(
                title = "Join Trip",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryColors.Primary50,
                            Color.White
                        )
                    )
                )
        ) {
            when (val state = uiState) {
                JoinTripUiState.Idle -> {
                    InviteCodeInput(
                        inviteCode = inviteCode,
                        onCodeChange = viewModel::onInviteCodeChange,
                        onValidate = viewModel::validateCode
                    )
                }

                JoinTripUiState.Loading -> {
                    LoadingScreen("Validating code...")
                }

                is JoinTripUiState.TripFound -> {
                    TripPreview(
                        trip = state.trip,
                        onJoinClick = viewModel::joinTrip,
                        onBackClick = viewModel::resetState
                    )
                }

                JoinTripUiState.Joining -> {
                    LoadingScreen("Joining trip...")
                }

                is JoinTripUiState.Error -> {
                    InviteCodeInput(
                        inviteCode = inviteCode,
                        onCodeChange = viewModel::onInviteCodeChange,
                        onValidate = viewModel::validateCode,
                        errorMessage = state.message
                    )
                }

                is JoinTripUiState.Success -> Unit
            }
        }
    }
}

@Composable
private fun InviteCodeInput(
    inviteCode: String,
    onCodeChange: (String) -> Unit,
    onValidate: () -> Unit,
    errorMessage: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PrimaryColors.Primary500,
            shadowElevation = 8.dp
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.padding(24.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enter Invite Code",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NeutralColors.Neutral900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ask the trip creator for the 8-character invite code",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralColors.Neutral600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = inviteCode,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Invite Code") },
            placeholder = { Text("ABC12345") },
            singleLine = true,
            isError = errorMessage != null,
            supportingText = if (errorMessage != null) {
                { Text(errorMessage, color = SemanticColors.Error) }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onValidate() }
            ),
            shape = CustomShapes.TextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColors.Primary600,
                focusedLabelColor = PrimaryColors.Primary600
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onValidate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = inviteCode.length == 8,
            shape = CustomShapes.ButtonLargeShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColors.Primary600
            )
        ) {
            Text(
                text = "Validate Code",
                style = CustomTextStyles.ButtonLarge
            )
        }
    }
}

@Composable
private fun TripPreview(
    trip: Trip,
    onJoinClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = SecondaryColors.Secondary100
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.padding(20.dp),
                tint = SecondaryColors.Secondary600
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Trip Found!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NeutralColors.Neutral900
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.CardShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeutralColors.Neutral900
                )

                if (trip.description != null && trip.description.isNotBlank()) {
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral600
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = PrimaryColors.Primary600
                    )
                    Text(
                        text = formatDateRange(trip.startDate, trip.endDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral700
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onJoinClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CustomShapes.ButtonLargeShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryColors.Secondary600
            )
        ) {
            Icon(
                imageVector = Icons.Default.GroupAdd,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Join This Trip",
                style = CustomTextStyles.ButtonLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onBackClick) {
            Text(
                text = "Try Different Code",
                color = NeutralColors.Neutral600
            )
        }
    }
}

private fun formatDateRange(
    startDate: java.time.LocalDate,
    endDate: java.time.LocalDate?
): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val start = startDate.format(formatter)
    return if (endDate != null) {
        val end = endDate.format(formatter)
        "$start - $end"
    } else {
        start
    }
}