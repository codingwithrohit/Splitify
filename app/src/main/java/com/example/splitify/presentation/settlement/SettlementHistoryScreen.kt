package com.example.splitify.presentation.settlement

import android.R.attr.padding
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.SettlementStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementHistoryScreen(
    tripId: String,
    currentMemberId: String,
    onBack: () -> Unit,
    viewModel: SettlementViewModel = hiltViewModel()
){
    val uiState by viewModel.settlementListState.collectAsStateWithLifecycle()

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
        topBar = {
            TopAppBar(
                title = { Text("Settlement History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ){
        paddingValues ->
        when(val state = uiState){
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
                if(state.settlements.isEmpty()){
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Settlements Yet",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Settlements will appear here",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }
                    }
                }
                else{
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        items(state.settlements){settlement ->
                            SettlementHistoryCard(
                                settlement = settlement,
                                currentMemberId = currentMemberId,
                                onConfirm = {showConfirmDialog = settlement}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementHistoryCard(
    settlement: SettlementWithMember,
    currentMemberId: String,
    onConfirm: () -> Unit
){
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    val isReceiver = settlement.toMember.id == currentMemberId
    val isPending = settlement.settlement.status == SettlementStatus.PENDING

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = when (settlement.settlement.status) {
                                SettlementStatus.PENDING -> "Pending"
                                SettlementStatus.CONFIRMED -> "Confirmed"
                                SettlementStatus.DISPUTED -> "Disputed"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (settlement.settlement.status) {
                                SettlementStatus.PENDING -> Icons.Default.Schedule
                                SettlementStatus.CONFIRMED -> Icons.Default.CheckCircle
                                SettlementStatus.DISPUTED -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (settlement.settlement.status) {
                            SettlementStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                            SettlementStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                            SettlementStatus.DISPUTED -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment details
            Text(
                text = "${settlement.fromMember.displayName} → ${settlement.toMember.displayName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = currencyFormat.format(settlement.settlement.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Notes
            settlement.settlement.notes?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = "Created: ${dateFormat.format(Date(settlement.settlement.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (settlement.settlement.settledAt != null) {
                Text(
                    text = "Confirmed: ${dateFormat.format(Date(settlement.settlement.settledAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Confirm button (only for receiver on pending settlements)
            if (isPending && isReceiver) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirm Receipt")
                }
            }
        }
    }
}