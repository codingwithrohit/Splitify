package com.example.splitify.presentation.settlement

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.presentation.balances.BalancesUiState
import com.example.splitify.presentation.balances.BalancesViewModel
import com.example.splitify.presentation.components.EmptySettlementsState
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import com.example.splitify.presentation.theme.md_theme_dark_warningContainer
import com.example.splitify.presentation.theme.md_theme_light_warningContainer
import com.example.splitify.util.CurrencyUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementHistoryScreen(
    tripId: String,
    currentMemberId: String,
    onBack: () -> Unit,
    viewModel: SettlementViewModel = hiltViewModel(),
    balanceViewModel: BalancesViewModel = hiltViewModel()
) {
    val uiState by viewModel.settlementListState.collectAsStateWithLifecycle()
    val balanceState by balanceViewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf<SettlementWithMember?>(null) }

    LaunchedEffect(tripId) {
        viewModel.loadSettlements(tripId)
    }

    showConfirmDialog?.let { settlement ->
        ConfirmPaymentDialog(
            fromMemberName = settlement.fromMember.displayName,
            toMemberName = settlement.toMember.displayName,
            amount = settlement.settlement.amount,
            onDismiss = { showConfirmDialog = null },
            onConfirm = {
                viewModel.confirmSettlement(
                    settlementId = settlement.settlement.id,
                    confirmingMemberId = currentMemberId
                )
                showConfirmDialog = null
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SplitifyAppBar(
                title = "Settlements History",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SettlementListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettlementListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            is SettlementListUiState.Success -> {
                if (state.settlements.isEmpty()) {

                    EmptySettlementsState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )

                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {

                        val hasPendingSettlements = state.settlements.any {
                            it.settlement.status == SettlementStatus.PENDING
                        }
                        val allBalancesZero = when (val bState = balanceState) {
                            is BalancesUiState.Success -> {
                                bState.simplifiedDebts.isEmpty()
                            }

                            else -> false
                        }

                        if (hasPendingSettlements && allBalancesZero) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = md_theme_light_warningContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = md_theme_dark_warningContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Note: Pending settlements exist but all balances are settled. These may be outdated.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = md_theme_dark_warningContainer
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.settlements) { settlement ->
                                SettlementHistoryCard(
                                    settlement = settlement,
                                    currentMemberId = currentMemberId,
                                    onConfirm = { showConfirmDialog = settlement }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettlementHistoryCard(
    settlement: SettlementWithMember,
    currentMemberId: String,
    onConfirm: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    val isReceiver = settlement.toMember.id == currentMemberId
    val isPending = settlement.settlement.status == SettlementStatus.PENDING

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (settlement.settlement.status) {
                        SettlementStatus.PENDING -> SecondaryColors.Secondary100
                        SettlementStatus.CONFIRMED -> PrimaryColors.Primary100
                        SettlementStatus.DISPUTED -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (settlement.settlement.status) {
                                SettlementStatus.PENDING -> Icons.Default.Schedule
                                SettlementStatus.CONFIRMED -> Icons.Default.CheckCircle
                                SettlementStatus.DISPUTED -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (settlement.settlement.status) {
                                SettlementStatus.PENDING -> SecondaryColors.Secondary700
                                SettlementStatus.CONFIRMED -> PrimaryColors.Primary700
                                SettlementStatus.DISPUTED -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = when (settlement.settlement.status) {
                                SettlementStatus.PENDING -> "Pending"
                                SettlementStatus.CONFIRMED -> "Confirmed"
                                SettlementStatus.DISPUTED -> "Disputed"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when (settlement.settlement.status) {
                                SettlementStatus.PENDING -> SecondaryColors.Secondary700
                                SettlementStatus.CONFIRMED -> PrimaryColors.Primary700
                                SettlementStatus.DISPUTED -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = PrimaryColors.Primary100
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = settlement.fromMember.displayName
                                    .firstOrNull()
                                    ?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryColors.Primary700,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = NeutralColors.Neutral400,
                        modifier = Modifier.size(20.dp)
                    )

                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = SecondaryColors.Secondary100
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = settlement.toMember.displayName
                                    .firstOrNull()
                                    ?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = SecondaryColors.Secondary700,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = CurrencyUtils.format(settlement.settlement.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColors.Primary600
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${settlement.fromMember.displayName} paid ${settlement.toMember.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral700
            )

            settlement.settlement.notes?.let { notes ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeutralColors.Neutral100
                ) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralColors.Neutral700,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NeutralColors.Neutral500,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Created: ${dateFormat.format(Date(settlement.settlement.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralColors.Neutral600
                    )
                }

                if (settlement.settlement.settledAt != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SecondaryColors.Secondary600,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Confirmed: ${dateFormat.format(Date(settlement.settlement.settledAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryColors.Secondary600
                        )
                    }
                }
            }

            if (isPending && isReceiver) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CustomShapes.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryColors.Secondary600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Receipt",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

