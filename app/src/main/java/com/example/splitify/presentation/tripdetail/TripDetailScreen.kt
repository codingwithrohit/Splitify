package com.example.splitify.presentation.tripdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.example.splitify.presentation.addmembers.EmptyState
import com.example.splitify.presentation.balances.BalancesScreen
import io.github.jan.supabase.realtime.Column
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.annotation.meta.When

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    onNavigateBack: () -> Unit,
    onAddExpense: () -> Unit,
    onAddMember: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    //var selectedTab by remember { mutableStateOf(0) }
    //val tabs = listOf("Overview", "Expenses", "Members", "Balances")

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
                    expenses = state.expenses,
                    members = state.members,
                    totalExpense = state.totalExpenses,
                    currentTab = state.currentTab,
                    onTabSelected = viewModel::selectTab,
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
    expenses: List<Expense>,
    members: List<TripMember>,
    totalExpense: Double,
    currentTab: TripDetailTab,
    onTabSelected: (TripDetailTab) -> Unit,
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
            TripDetailTab.EXPENSES -> ExpensesTab(expenses = expenses)
            TripDetailTab.MEMBERS -> MembersTab(members = members)
            TripDetailTab.BALANCES -> BalancesTab(trip.id)
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
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No expenses yet",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Tap + to add your first expense",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = expenses,
                key = { expense -> expense.id }
            ) { expense ->
                ExpenseCard(expense = expense)
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category icon
                Text(
                    text = expense.category.icon,
                    style = MaterialTheme.typography.headlineMedium
                )

                Column {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Paid by ${expense.paidByName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = expense.expenseDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.isGroupExpense) {
                        Text(
                            text = "Group expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Amount
            Text(
                text = "₹%.2f".format(expense.amount),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
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
    tripId: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BalancesScreen(
            onNavigateToSettlement = { fromId, toId, amount ->
                // TODO: Navigate to settlement screen
            }
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