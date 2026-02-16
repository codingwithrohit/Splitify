package com.example.splitify.presentation.expense

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.usecase.expense.CanModifyExpenseUseCase
import com.example.splitify.presentation.components.EmptyExpensesState
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.components.ExpensesLoadingSkeleton
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.CategoryColors
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors
import com.example.splitify.util.CurrencyUtils
import com.example.splitify.util.getCategoryIcon
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpensesScreen(
    currentMemberId: String?,
    currentUserId: String?,
    navController: NavController,
    onBack: () -> Unit,
    onEditExpense: (String) -> Unit,
    onAddExpense: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Group Expense", "Personal Expense")
    val pagerState = rememberPagerState(pageCount = {tabs.size} )
    val coroutineState = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }



    LaunchedEffect(navBackStackEntry) {
        val targetTab = navBackStackEntry?.savedStateHandle?.get<Int>("target_tab")
        if (targetTab != null) {
            pagerState.animateScrollToPage(targetTab)
            navBackStackEntry?.savedStateHandle?.remove<Int>("target_tab")
        }
    }

    expenseToDelete?.let { expense ->
        DeleteExpenseDialog(
            expense = expense,
            onConfirm = {
                viewModel.deleteExpense(expense.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SplitifyAppBar(
                title = "Expenses",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            val state = uiState
            if (state is ExpenseUiState.Success && state.expenses.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddExpense,
                    shape = CircleShape,
                    containerColor = PrimaryColors.Primary600,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is ExpenseUiState.Loading -> {

                ExpensesLoadingSkeleton()
            }

            is ExpenseUiState.Success -> {
                if (state.expenses.isEmpty()) {
                    EmptyExpensesState(
                        onAddExpense = onAddExpense,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                } else {

                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)) {

                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = Color.White,
                            indicator = {tabPositions ->
                                if(pagerState.currentPage < tabs.size){
                                    SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                        height = 3.dp,
                                        color = PrimaryColors.Primary600
                                    )
                                }
                            },
                            divider = { Divider(color = NeutralColors.Neutral100) }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val selected = pagerState.currentPage == index
                                Tab(
                                    selected = selected,
                                    onClick = { coroutineState.launch { pagerState.animateScrollToPage(index) } },
                                    text = { Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if(selected) PrimaryColors.Primary600 else NeutralColors.Neutral600
                                    ) }
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { page ->

                            val filteredExpense = when(page){
                                0 -> state.expenses.filter { it.isGroupExpense }
                                1 -> state.expenses.filter { !it.isGroupExpense }
                                else -> emptyList()
                            }
                            when (page) {
                                0 -> ListExpense(
                                    expense = filteredExpense,
                                    members = state.members,
                                    currentMemberId = currentMemberId,
                                    currentUserId = currentUserId,
                                    onEditExpense = onEditExpense,
                                    onDeleteExpense = { expenseToDelete = it}
                                )
                                1 -> ListExpense(
                                    expense = filteredExpense,
                                    members = state.members,
                                    currentMemberId = currentMemberId,
                                    currentUserId = currentUserId,
                                    onEditExpense = onEditExpense,
                                    onDeleteExpense = { expenseToDelete = it }
                                )
                            }
                        }
                    }
                }
            }

            is ExpenseUiState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = { },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }

    }
}

@Composable
fun ListExpense(
    expense: List<Expense>,
    members: List<TripMember>,
    currentMemberId: String?,
    currentUserId: String?,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (Expense) -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = expense,
            key = { it.id }
        ) { expense ->
            ExpenseCard(
                expense = expense,
                paidByMember = members.find { it.id == expense.paidBy },
                currentUserMember = members.find { it.id == currentMemberId },
                currentUserId = currentUserId,
                onEdit = { onEditExpense(expense.id) },
                onDelete = {  onDeleteExpense(expense)  }
            )
        }
    }
}

@Composable
fun DeleteExpenseDialog(
    expense: Expense,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Expense?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete this expense?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeutralColors.Neutral700
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = CustomShapes.CardShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralColors.Neutral900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.format(expense.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${expense.category.icon} ${expense.category.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralColors.Neutral600
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
                ),
                shape = CustomShapes.ButtonShape
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = CustomShapes.DialogShape
    )
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    paidByMember: TripMember?,
    currentUserMember: TripMember?,
    currentUserId: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canModifyExpenseUseCase: CanModifyExpenseUseCase = remember { CanModifyExpenseUseCase() }
) {
    val canModify = canModifyExpenseUseCase(
        expense = expense,
        currentUserMember = currentUserMember,
        currentUserId = currentUserId
    )

    val categoryColor = when (expense.category) {
        Category.FOOD -> CategoryColors.Food
        Category.TRANSPORT -> CategoryColors.Transport
        Category.ACCOMMODATION -> CategoryColors.Accommodation
        Category.ENTERTAINMENT -> CategoryColors.Entertainment
        Category.SHOPPING -> CategoryColors.Shopping
        Category.OTHER -> CategoryColors.Other
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = CustomShapes.CardShape,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // ============================================
                // LEFT SIDE: Icon + Details (FIXED)
                // ============================================
                Row(
                    modifier = Modifier.weight(1f),  // ✅ Prevents overflow
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)  // ✅ Consistent spacing
                ) {
                    // Category Icon
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(expense.category),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Description, Paid By, Date (FIXED - separate lines)
                    Column(
                        modifier = Modifier.weight(1f),  // ✅ Flexible width
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Description
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralColors.Neutral900,
                            maxLines = 1,  // ✅ FIX: Single line only
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis  // ✅ FIX: Show "..."
                        )

                        // Paid by (FIXED - handles long names)
                        Text(
                            text = "Paid by ${paidByMember?.displayName ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralColors.Neutral600,
                            maxLines = 1,  // ✅ FIX: Prevents overflow
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis  // ✅ FIX: Truncates long names
                        )

                        // Date (FIXED - separate line, always visible)
                        Text(
                            text = expense.expenseDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),  // ✅ FIX: Full date
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralColors.Neutral600
                        )
                    }
                }

                // ============================================
                // RIGHT SIDE: Amount + Category (FIXED SPACING)
                // ============================================
                Column(
                    modifier = Modifier.padding(start = 16.dp),  // ✅ FIX: More space from left
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)  // ✅ FIX: Better spacing
                ) {
                    // Amount
                    Text(
                        text = CurrencyUtils.format(expense.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColors.Primary600
                    )

                    // Category Badge (FIXED - better spacing)
                    Surface(
                        shape = RoundedCornerShape(6.dp),  // ✅ FIX: Slightly rounded
                        color = categoryColor.copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 4.dp)  // ✅ FIX: Extra space from amount
                    ) {
                        Text(
                            text = expense.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium,  // ✅ FIX: Slightly bolder
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Edit/Delete buttons (unchanged)
            if (canModify) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEdit,
                        shape = CustomShapes.ButtonShape,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PrimaryColors.Primary600
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Edit",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryColors.Primary600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onDelete,
                        shape = CustomShapes.ButtonShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SemanticColors.Error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Delete",
                            style = MaterialTheme.typography.labelLarge,
                            color = SemanticColors.Error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//private fun ExpenseCard(
//    expense: Expense,
//    paidByMember: TripMember?,
//    currentUserMember: TripMember?,
//    currentUserId: String?,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit,
//    canModifyExpenseUseCase: CanModifyExpenseUseCase = remember { CanModifyExpenseUseCase() }
//) {
//    val canModify = canModifyExpenseUseCase(
//        expense = expense,
//        currentUserMember = currentUserMember,
//        currentUserId = currentUserId
//    )
//
//    val categoryColor = when (expense.category) {
//        Category.FOOD -> CategoryColors.Food
//        Category.TRANSPORT -> CategoryColors.Transport
//        Category.ACCOMMODATION -> CategoryColors.Accommodation
//        Category.ENTERTAINMENT -> CategoryColors.Entertainment
//        Category.SHOPPING -> CategoryColors.Shopping
//        Category.OTHER -> CategoryColors.Other
//    }
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .animateContentSize(),
//        shape = CustomShapes.CardShape,
//        color = Color.White,
//        shadowElevation = 2.dp
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Row(
//                    modifier = Modifier.weight(1f),
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Surface(
//                        modifier = Modifier.size(48.dp),
//                        shape = CircleShape,
//                        color = categoryColor.copy(alpha = 0.15f)
//                    ) {
//                        Icon(
//                            imageVector = getCategoryIcon(expense.category),
//                            contentDescription = null,
//                            tint = categoryColor,
//                            modifier = Modifier.padding(12.dp)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Column {
//                        Text(
//                            text = expense.description,
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.SemiBold,
//                            color = NeutralColors.Neutral900
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(4.dp)
//                        ) {
//                            Text(
//                                text = "Paid by ${paidByMember?.displayName ?: "Unknown"}",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = NeutralColors.Neutral600
//                            )
//                            Text(
//                                text = "•",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = NeutralColors.Neutral400
//                            )
//                            Text(
//                                text = expense.expenseDate.format(DateTimeFormatter.ofPattern("MMM dd")),
//                                style = MaterialTheme.typography.bodySmall,
//                                color = NeutralColors.Neutral600
//                            )
//                        }
//                    }
//                }
//
//                Column(horizontalAlignment = Alignment.End) {
//                    Text(
//                        text = CurrencyUtils.format(expense.amount),
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold,
//                        color = PrimaryColors.Primary600
//                    )
//                    Surface(
//                        shape = RoundedCornerShape(4.dp),
//                        color = categoryColor.copy(alpha = 0.1f)
//                    ) {
//                        Text(
//                            text = expense.category.displayName,
//                            style = MaterialTheme.typography.labelSmall,
//                            color = categoryColor,
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                        )
//                    }
//                }
//            }
//
//            if (canModify) {
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    TextButton(
//                        onClick = onEdit,
//                        shape = CustomShapes.ButtonShape,
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Edit,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp),
//                            tint = PrimaryColors.Primary600
//                        )
//                        Spacer(modifier = Modifier.width(6.dp))
//                        Text(
//                            "Edit",
//                            style = MaterialTheme.typography.labelLarge,
//                            color = PrimaryColors.Primary600,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    TextButton(
//                        onClick = onDelete,
//                        shape = CustomShapes.ButtonShape
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp),
//                            tint = SemanticColors.Error
//                        )
//                        Spacer(modifier = Modifier.width(6.dp))
//                        Text(
//                            "Delete",
//                            style = MaterialTheme.typography.labelLarge,
//                            color = SemanticColors.Error,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//}