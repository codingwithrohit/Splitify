package com.example.splitify.presentation.expense

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.usecase.expense.CanModifyExpenseUseCase
import com.example.splitify.presentation.components.EmptyExpensesState
import com.example.splitify.presentation.components.ErrorStateWithRetry
import com.example.splitify.presentation.components.ExpensesLoadingSkeleton
import com.example.splitify.util.SnackbarController
import java.text.NumberFormat
import java.util.Locale

/**
 * Full-screen Expenses list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    currentMemberId: String?,
    currentUserId: String?,
    onBack: () -> Unit,
    onEditExpense: (String) -> Unit,
    onAddExpense: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarController = remember(snackbarHostState, scope) {
        SnackbarController(snackbarHostState, scope)
    }

    // Delete confirmation
    expenseToDelete?.let { expense ->
        DeleteExpenseDialog(
            expense = expense,
            onConfirm = {
                viewModel.deleteExpense(expense.id)
                expenseToDelete = null
                snackbarController.showSuccess("Expense deleted")
            },
            onDismiss = { expenseToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.expenses,
                            key = { it.id }
                        ) { expense ->
                            ExpenseCard(
                                expense = expense,
                                paidByMember = state.members.find { it.id == expense.paidBy },
                                currentUserMember = state.members.find { it.id == currentMemberId },
                                currentUserId = currentUserId,
                                onEdit = { onEditExpense(expense.id) },
                                onDelete = { expenseToDelete = expense }
                            )
                        }

                        // Bottom padding for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            is ExpenseUiState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = { /* Auto-retry via Flow */ },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
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
private fun ExpenseCard(
    expense: Expense,
    paidByMember: TripMember?,
    currentUserMember: TripMember?,  // ✅ Pass full member object
    currentUserId: String?,           // ✅ Pass user ID
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canModifyExpenseUseCase: CanModifyExpenseUseCase = remember { CanModifyExpenseUseCase() }
) {
    // ✅ Proper permission check
    val canModify = canModifyExpenseUseCase(
        expense = expense,
        currentUserMember = currentUserMember,
        currentUserId = currentUserId
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ... expense details ...

            // ✅ Show edit/delete buttons if user has permission
            if (canModify) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
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