package com.example.splitify.presentation.insights

import android.os.Build
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
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
            Surface(
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Trip Insights",
                            fontWeight = FontWeight.Bold,
                            color = NeutralColors.Neutral900
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeutralColors.Neutral100)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = NeutralColors.Neutral700
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.generateSummary { summary ->
                                    summaryText = summary
                                    showSummaryDialog = true
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share Summary",
                                tint = PrimaryColors.Primary600
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is InsightsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryColors.Primary500
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
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = PrimaryColors.Primary100
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.padding(32.dp),
                tint = PrimaryColors.Primary600
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Expenses Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NeutralColors.Neutral900
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add some expenses to see insights",
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralColors.Neutral600
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
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TripHeaderCard(insights)
        }

        item {
            StatsCardsGrid(insights)
        }

        item {
            CategoryBreakdownSection(insights)
        }

        item {
            MemberSpendingSection(insights)
        }

        item {
            DailySpendingSection(insights)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TripHeaderCard(insights: TripInsights) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.CardElevatedShape,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryColors.Primary500,
                            PrimaryColors.Primary700
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = insights.tripName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                val dateRange = if (insights.endDate != null) {
                    "${insights.startDate.format(dateFormatter)} - ${insights.endDate.format(dateFormatter)}"
                } else {
                    "Started ${insights.startDate.format(dateFormatter)}"
                }

                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${insights.tripDurationDays} days • ${insights.totalMembers} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun StatsCardsGrid(insights: TripInsights) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Spent",
                value = CurrencyUtils.format(insights.totalSpending),
                icon = Icons.Default.AttachMoney,
                color = SecondaryColors.Secondary500,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Expenses",
                value = insights.totalExpenses.toString(),
                icon = Icons.Default.Receipt,
                color = PrimaryColors.Primary500,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Per Person",
                value = CurrencyUtils.format(insights.averagePerPerson),
                icon = Icons.Default.Person,
                color = PrimaryColors.Primary600,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Per Day",
                value = CurrencyUtils.format(
                    insights.totalSpending / insights.tripDurationDays
                ),
                icon = Icons.Default.CalendarToday,
                color = SecondaryColors.Secondary600,
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
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900
            )
        }
    }
}