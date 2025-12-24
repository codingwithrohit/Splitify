package com.example.splitify.presentation.tripdetail

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Trip
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.usecase.expense.CanModifyExpenseUseCase
import com.example.splitify.presentation.addmembers.EmptyState
import com.example.splitify.presentation.balances.BalancesScreen
import com.example.splitify.presentation.expense.ExpenseUiState
import com.example.splitify.presentation.expense.ExpenseViewModel
import com.example.splitify.util.getCategoryIcon
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    onNavigateBack: () -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onAddMember: () -> Unit,
    onNavigateToSettlementHistory: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel(),
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentMemberId by viewModel.currentMemberId.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is TripDetailUiState.Success -> {
                            Text(
                                text = state.trip.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        else -> {
                            Text("Trip Details")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val state = uiState
            if(state is TripDetailUiState.Success){
                when(state.currentTab){
                    TripDetailTab.EXPENSES -> {
                        FloatingActionButton(onClick = onAddExpense) {
                            Icon(Icons.Default.Add, contentDescription = "Add Expense")
                        }
                    }
                    TripDetailTab.MEMBERS -> {
                        FloatingActionButton(onClick = onAddMember) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                        }
                    }
                    else -> Unit
                }
            }
        }
    ) { paddingValues ->

        when(val state = uiState){
            is TripDetailUiState.Loading ->{
                LoadingContent(modifier = Modifier.padding(paddingValues))
            }
            is TripDetailUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is TripDetailUiState.Success -> {
                TripDetailContent(
                    trip = state.trip,
                    onEditExpense = onEditExpense,
                    members = state.members,
                    totalExpense = state.totalExpenses,
                    currentTab = state.currentTab,
                    onTabSelected = viewModel::selectTab,
                    currentMemberId = currentMemberId,
                    onNavigateToHistory = onNavigateToSettlementHistory,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun TripDetailContent(
    trip: Trip,
    onEditExpense: (String) -> Unit,
    members: List<TripMember>,
    totalExpense: Double,
    currentTab: TripDetailTab,
    onTabSelected: (TripDetailTab) -> Unit,
    currentMemberId: String?,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier.fillMaxSize()) {

        TripSummaryCard(
            trip = trip,
            totalExpense = totalExpense,
            memberCount = members.size,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = currentTab.ordinal) {
            TripDetailTab.entries.forEach { tab ->
                Tab(
                    selected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }

        when(currentTab){
            TripDetailTab.OVERVIEW -> OverviewTab(trip)
            TripDetailTab.EXPENSES -> ExpensesTab(
                tripId = trip.id,
                currentMemberId = currentMemberId,
                onEditExpense = onEditExpense,
                currentUserId = currentMemberId
            )
            TripDetailTab.MEMBERS -> MembersTab(members = members)
            TripDetailTab.BALANCES -> BalancesTab(trip.id, currentMemberId = currentMemberId, onNavigateToHistory = onNavigateToHistory)
        }
    }
}

@Composable
fun TripSummaryCard(
    trip: Trip,
    totalExpense: Double,
    memberCount: Int,
    modifier: Modifier = Modifier
){
    Card(modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //Dates
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Column {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.startDate.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy")
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                //If end date is not null
                if(trip.endDate != null){
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "End Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trip.endDate.format(
                                DateTimeFormatter.ofPattern("MMM dd, yyyy")
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            //StatsRow
            Row(horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ){
                StatItem(
                    label = "Total",
                    value = "₹%.2f".format(totalExpense)
                )
                StatItem(
                    label = "Members",
                    value = memberCount.toString()
                )
                StatItem(
                    label = "Per Person",
                    value = if(memberCount>0)
                        "₹%.2f".format(totalExpense / memberCount)
                    else
                        "₹0.00"
                )

            }

        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverviewTab(trip: Trip) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (trip.description != null) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = trip.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Invite Code",
            style = MaterialTheme.typography.titleMedium
        )
        Card {
            Text(
                text = trip.inviteCode,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ExpensesTab(
    tripId: String,
    currentMemberId: String?,
    currentUserId: String?,
    onEditExpense: (String) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    LaunchedEffect(tripId) {
        viewModel.loadExpenses(tripId)
    }

    expenseToDelete?.let { expense ->
        DeleteExpenseDialog(
            expense = expense,
            onConfirm = {
                viewModel.deleteExpense(expense.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null}
        )
    }

    when(val state = uiState){
        is ExpenseUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ExpenseUiState.Error -> {
            ErrorContent(message = state.message, onRetry = {})
        }
        is ExpenseUiState.Success -> {
            val currentMember = state.members.find { it.id == currentMemberId }
            if(state.expenses.isEmpty()){
                EmptyState(
                    message = "No expenses yet.\n Tap + to add your first member",
                    icon = Icons.Default.Add
                )
            }
            else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.expenses){ expense ->
                        ExpenseCard(
                            expense = expense,
                            paidByMember = state.members.find { it.id == expense.paidBy },
                            currentUserMember = currentMember,
                            currentUserId = currentUserId,
                            onEdit = {onEditExpense(expense.id)},
                            onDelete = {expenseToDelete = expense}
                        )
                    }
                }
            }
        }
    }

}


@Composable
private fun ExpenseCard(
    expense: Expense,
    paidByMember: TripMember?,
    currentUserMember: TripMember?,
    currentUserId: String?,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    canModifyExpenseUseCase: CanModifyExpenseUseCase = remember { CanModifyExpenseUseCase() }
) {
    val canModify = canModifyExpenseUseCase(
        expense = expense,
        currentUserMember = currentUserMember,
        currentUserId = currentUserId
    )
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Expense header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Paid by ${paidByMember?.displayName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "₹${expense.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category and date
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(expense.category.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(expense.category),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Text(
                    text = expense.expenseDate.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ Edit/Delete buttons (only if user has permission)
            if (canModify) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,

                ) {
                    OutlinedButton(
                        onClick = {
                            Log.d("EDIT_CLICK", "Edit clicked for expenseId=${expense.id}")
                            onEdit(expense.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersTab(
    members: List<TripMember>
) {
    Box(
        modifier = Modifier.fillMaxSize(),

    ) {
        if(members.isEmpty()){
            EmptyState(
                message = "No members yet.\n Tap + to add your first member",
                icon = Icons.Default.Add
            )
        }
        else{
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            )
            {
                item {
                    Text(
                        text = "${members.size} member${if(members.size > 1) "s" else ""} ",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(members,{it.id}){ member ->
                    MemberCard(member)
                }
            }
        }
    }
}

@Composable
fun DeleteExpenseDialog(
    expense: Expense,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text("Delete Expense?")
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete this expense?",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Show expense details
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currencyFormat.format(expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = expense.category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This action cannot be undone. All split data will also be deleted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MemberCard(member: TripMember) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (member.isAdmin)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.displayName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (member.isAdmin)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (member.isAdmin) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Admin",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (member.isAdmin) "Admin" else "Member",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (member.isGuest) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Guest",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

fun Long.toFormattedDate(
    pattern: String = "MMM dd, yyyy"
): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern(pattern))
}


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
private fun BalancesTab(
    tripId: String,
    currentMemberId: String?,
    onNavigateToHistory: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BalancesScreen(
            tripId = tripId,
            currentMemberId = currentMemberId!!,
            onNavigateToHistory = onNavigateToHistory
        )
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun LoadingContent(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){

        CircularProgressIndicator()
    }
}