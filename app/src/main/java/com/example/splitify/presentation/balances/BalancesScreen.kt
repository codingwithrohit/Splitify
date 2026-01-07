
package com.example.splitify.presentation.balances

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.Balance
import com.example.splitify.domain.model.Settlement
import com.example.splitify.domain.model.SettlementStatus
import com.example.splitify.domain.model.SimplifiedDebt
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.components.AllSettledState
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.settlement.CancelSettlementDialog
import com.example.splitify.presentation.settlement.SettleUpDialog
import com.example.splitify.presentation.settlement.SettlementUiState
import com.example.splitify.presentation.settlement.SettlementViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun BalancesScreen(
    tripId: String,
    currentMemberId: String,
    onNavigateToHistory: () -> Unit = {},
    viewModel: BalancesViewModel = hiltViewModel(),
    settlementViewModel: SettlementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settlementState by settlementViewModel.uiState.collectAsStateWithLifecycle()

    var selectedDebt by remember { mutableStateOf<Pair<SimplifiedDebt, Pair<TripMember, TripMember>>?>(null) }
    var settlementToCancel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(settlementState) {
        if (settlementState is SettlementUiState.Success) {
            settlementViewModel.resetSettlementState()
            selectedDebt = null
        }
    }

    // Show settle up dialog
    selectedDebt?.let { (debt, members) ->
        SettleUpDialog(
            debt = debt,
            fromMember = members.first,
            toMember = members.second,
            isAdmin = (uiState as? BalancesUiState.Success)
                ?.memberBalances
                ?.find { it.member.id == currentMemberId }
                ?.member
                ?.isAdmin ?: false,
            onDismiss = { selectedDebt = null },
            onConfirm = { amount, notes, confirmOnBehalf ->
                settlementViewModel.createSettlement(
                    tripId = tripId,
                    fromMemberId = debt.fromMemberId,
                    toMemberId = debt.toMemberId,
                    amount = amount,
                    notes = notes,
                    confirmOnBehalf = confirmOnBehalf
                )
            }
        )
    }

    settlementToCancel?.let { settlementId ->
        CancelSettlementDialog(
            onDismiss = { settlementToCancel = null },
            onConfirm = {
                settlementViewModel.cancelSettlement(settlementId, currentMemberId)
                settlementToCancel = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Balances",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                },
                actions = {
                    // History button
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Settlement History"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) {padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)){
            when (val state = uiState) {
                is BalancesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BalancesUiState.Success -> {
                    if (state.simplifiedDebts.isEmpty()) {
                        // All settled!
                        AllSettledState(
                            onViewHistory = onNavigateToHistory,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        BalancesContent(
                            memberBalances = state.memberBalances,
                            simplifiedDebts = state.simplifiedDebts,
                            settlements = state.settlements,
                            currentMemberId = currentMemberId,
                            onSettleUp = { debt, fromMember, toMember ->
                                selectedDebt = debt to (fromMember to toMember)
                            },
                            onCancelSettlement = { settlementId ->
                                settlementToCancel = settlementId
                            },
                            onConfirmSettlement = { settlementId ->
                                settlementViewModel.confirmSettlement(settlementId, currentMemberId)
                            }
                        )
                    }
                }

                is BalancesUiState.Error -> {
                    ErrorStateWithRetry(
                        message = state.message,
                        onRetry = {viewModel::retry},
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }

        }
    }


}

@Composable
private fun BalancesContent(
    memberBalances: List<Balance>,
    simplifiedDebts: List<SimplifiedDebt>,
    settlements: List<Settlement>,
    currentMemberId: String,
    onSettleUp: (SimplifiedDebt, TripMember, TripMember) -> Unit,
    onCancelSettlement: (String) -> Unit,
    onConfirmSettlement: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        items(memberBalances) { balance ->
            BalanceCard(balance = balance)
        }

        // Section: Simplified Settlements
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Suggested Settlements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Minimum transactions needed to settle all debts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(simplifiedDebts) { debt ->

            val fromMember = memberBalances.find { it.member.id == debt.fromMemberId }?.member
            val toMember = memberBalances.find { it.member.id == debt.toMemberId }?.member

            if (fromMember != null && toMember != null) {

                val settlementState = getSettlementState(
                    debt = debt,
                    settlements = settlements,
                    currentMemberId = currentMemberId,
                    toMember = toMember
                )

                SimplifiedDebtCard(
                    debt = debt,
                    fromMember = fromMember,
                    toMember = toMember,
                    currentMemberId = currentMemberId,
                    settlementState = settlementState,
                    onSettleUp = { onSettleUp(debt, fromMember, toMember) },
                    onCancelSettlement = onCancelSettlement,
                    onConfirmSettlement = onConfirmSettlement
                )
            }
        }
    }
}

private fun getSettlementState(
    debt: SimplifiedDebt,
    settlements: List<Settlement>,
    currentMemberId: String,
    toMember: TripMember
): SettlementButtonState {
    // Find any settlement between these two members
    val settlement = settlements.firstOrNull { s ->
        (s.fromMemberId == debt.fromMemberId && s.toMemberId == debt.toMemberId) ||
                (s.fromMemberId == debt.toMemberId && s.toMemberId == debt.fromMemberId)
    }
//    s.fromMemberId == debt.fromMemberId && s.toMemberId == debt.toMemberId
    return when {
        settlement == null -> {
            SettlementButtonState.NoSettlement
        }
        settlement.status == SettlementStatus.PENDING -> {
            SettlementButtonState.Pending(
                settlementId = settlement.id,
                amount = settlement.amount,
                canConfirm = currentMemberId == debt.toMemberId,
                canCancel = currentMemberId == settlement.fromMemberId,  //debt.fromMemberId
                isGuest = toMember.isGuest
            )
        }
        settlement.status == SettlementStatus.CONFIRMED -> {
            SettlementButtonState.Confirmed(
                amount = settlement.amount,
                date = settlement.settledAt ?: settlement.createdAt
            )
        }
        else -> SettlementButtonState.NoSettlement
    }
}

@Composable
private fun BalanceCard(balance: Balance) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                balance.netBalance > 0 -> MaterialTheme.colorScheme.primaryContainer
                balance.netBalance < 0 -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = balance.member.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(balance.totalPaid),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Owes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(balance.totalOwed),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(balance.netBalance),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            balance.netBalance > 0 -> MaterialTheme.colorScheme.primary
                            balance.netBalance < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimplifiedDebtCard(
    debt: SimplifiedDebt,
    fromMember: TripMember,
    toMember: TripMember,
    currentMemberId: String,
    settlementState: SettlementButtonState,
    onSettleUp: () -> Unit,
    onCancelSettlement: (String) -> Unit,
    onConfirmSettlement: (String) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val isCurrentUserDebtor = fromMember.id == currentMemberId

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUserDebtor) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${fromMember.displayName} → ${toMember.displayName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormat.format(debt.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUserDebtor) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }

            when (settlementState) {
                is SettlementButtonState.NoSettlement -> {
                    if (isCurrentUserDebtor) {
                        Button(
                            onClick = onSettleUp,
                            modifier = Modifier.wrapContentWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Payment, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Settle Up")
                        }
                    }
                }

                is SettlementButtonState.Pending -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Info chip
                        AssistChip(
                            onClick = {},
                            label = { Text("Settlement Pending") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Schedule,
                                    null,
                                    Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )

                        Text(
                            text = "${currencyFormat.format(settlementState.amount)} payment pending confirmation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (settlementState.canConfirm) {
                                Button(
                                    onClick = { onConfirmSettlement(settlementState.settlementId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Confirm")
                                }
                            }

                            if (settlementState.canCancel) {
                                OutlinedButton(
                                    onClick = { onCancelSettlement(settlementState.settlementId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }

                is SettlementButtonState.Confirmed -> {
                    AssistChip(
                        onClick = {},
                        label = { Text("Settled ✓") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBalancesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All Settled!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No pending settlements",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
