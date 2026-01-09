package com.example.splitify.presentation.insights

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.TripInsights
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.insights.charts.CategoryBreakdownSection
import com.example.splitify.presentation.insights.charts.DailySpendingSection
import com.example.splitify.presentation.insights.charts.MemberSpendingSection
import com.example.splitify.util.CurrencyUtils
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSummaryDialog by remember { mutableStateOf(false) }
    var summaryText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trip Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.generateSummary { summary ->
                            summaryText = summary
                            showSummaryDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share Summary")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is InsightsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is InsightsUiState.Error -> {
                    ErrorStateWithRetry(
                        message = state.message,
                        onRetry = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is InsightsUiState.Success -> {
                    if (state.insights.totalExpenses == 0) {
                        EmptyInsightsState(modifier = Modifier.fillMaxSize())
                    } else {
                        InsightsContent(
                            insights = state.insights,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
    if (showSummaryDialog) {
        SummaryDialog(
            summary = summaryText,
            onDismiss = { showSummaryDialog = false }
        )
    }
}

@Composable
private fun EmptyInsightsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Expenses Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add some expenses to see insights",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun InsightsContent(
    insights: TripInsights,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trip Header
        item {
            TripHeaderCard(insights)
        }

        // Stats Cards Grid
        item {
            StatsCardsGrid(insights)
        }

        // Category Breakdown Section (Commit 4)
        item {
            CategoryBreakdownSection(insights)
        }
        item {
            // CategoryPieChart(insights) - Will add in Commit 4
            Text("Chart coming in Commit 4...")
        }

        // Member Spending Section (Commit 5)
        item {
            MemberSpendingSection(insights)
        }
        item {
            // MemberBarChart(insights) - Will add in Commit 5
            Text("Chart coming in Commit 5...")
        }

        // Daily Trend Section (Commit 6)
        item {
            DailySpendingSection(insights)
        }
        item {
            // DailyLineChart(insights) - Will add in Commit 6
            Text("Chart coming in Commit 6...")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TripHeaderCard(insights: TripInsights) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = insights.tripName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val dateRange = if (insights.endDate != null) {
                "${insights.startDate.format(dateFormatter)} - ${insights.endDate.format(dateFormatter)}"
            } else {
                "Started ${insights.startDate.format(dateFormatter)}"
            }

            Text(
                text = dateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${insights.tripDurationDays} days • ${insights.totalMembers} members",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatsCardsGrid(insights: TripInsights) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Spent",
                value = CurrencyUtils.format(insights.totalSpending),
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Expenses",
                value = insights.totalExpenses.toString(),
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Per Person",
                value = CurrencyUtils.format(insights.averagePerPerson),
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Per Day",
                value = CurrencyUtils.format(
                    insights.totalSpending / insights.tripDurationDays
                ),
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}