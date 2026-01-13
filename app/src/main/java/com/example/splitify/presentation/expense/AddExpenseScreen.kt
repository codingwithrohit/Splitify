package com.example.splitify.presentation.expense

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.components.FailureToast
import com.example.splitify.presentation.components.InlineErrorMessage
import com.example.splitify.presentation.components.LoadingButton
import com.example.splitify.presentation.components.SuccessToast
import com.example.splitify.util.CurrencyUtils
import com.example.splitify.util.SnackbarController
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.exp


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigationBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isGroupExpense by viewModel.isGroupExpense.collectAsStateWithLifecycle()
    val selectedMemberIds by viewModel.selectedMemberIds.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessToast by remember { mutableStateOf(false) }
    var showFailureToast by remember { mutableStateOf(false) }

    val message = when(viewModel.mode){
        is ExpenseFormMode.Add -> "Expense added Successfully"
        is ExpenseFormMode.Edit -> "Expense updated Successfully"
    }

    LaunchedEffect(uiState.isSaved) {
        if(uiState.isSaved){
            showSuccessToast = true
            delay(1500)
            onNavigationBack()
        }
    }
    LaunchedEffect(uiState.amountError) {
        uiState.amountError?.let { error ->
            if (error.isNotBlank()) {
                showFailureToast = true
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text(
                when(viewModel.mode){
                    is ExpenseFormMode.Add -> "Add Expense"
                    is ExpenseFormMode.Edit -> "Edit Expense"
                }
            ) },
            navigationIcon = { IconButton(onClick = onNavigationBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            } }
        ) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        if(uiState.isLoading && uiState.members.isEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Amount
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    isError = uiState.amountError != null,
                    label = { Text("Amount *") },
                    placeholder = { Text("0.0") },
                    prefix = { Text("₹") },
                    supportingText = uiState.amountError?.let{ { InlineErrorMessage(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                //Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description *") },
                    placeholder = { Text("What was this expense for?") },
                    isError = uiState.descriptionError != null,
                    supportingText = {
                        if (uiState.descriptionError != null) {
                            uiState.descriptionError?.let { InlineErrorMessage(it) }
                        } else {
                            Text(
                                text = "${uiState.description.length}/50 characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    ,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()}),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    enabled = !uiState.isLoading
                )

                //Category
                CategorySelector(
                    selectedCategory = uiState.category,
                    onCategorySelected = viewModel::onCategoryChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                //Date
                DatePickerField(
                    label = "Date *",
                    date = uiState.expenseDate,
                    onDateChange = viewModel::onDateChange,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                //Who Paid
                PaidBySelector(
                    members = uiState.members,
                    selectedMemberId = uiState.paidByMemberId,
                    onMemberSelected = viewModel::onPaidByChange,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Split Type
                Text(
                    text = "Split Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !viewModel.isGroupExpense.value,
                        onClick = { viewModel.setIsGroupExpense(false) },
                        label = { Text("Personal") },
                        leadingIcon = if (!viewModel.isGroupExpense.value) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = viewModel.isGroupExpense.value,
                        onClick = { viewModel.setIsGroupExpense(true) },
                        label = { Text("Group") },
                        leadingIcon = if (viewModel.isGroupExpense.value) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Member Selection (only for group expenses)
                if (isGroupExpense) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Split With",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // FIXED: No longer checking for AddExpenseUiState.Success
                    // Just check if members list is not empty
                    if (uiState.members.isNotEmpty()) {
                        MemberSelectionList(
                            members = uiState.members,
                            selectedMemberIds = selectedMemberIds,
                            onMemberToggle = viewModel::toggleMember
                        )

                        // Show split preview
                        if (selectedMemberIds.isNotEmpty() && uiState.amount.isNotBlank()) {
                            val amount = uiState.amount.toDoubleOrNull() ?: 0.0
                            val splitAmount = amount / selectedMemberIds.size

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Each person owes:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = CurrencyUtils.format(splitAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else if (uiState.isLoading) {
                        // Show loading while members are being fetched
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading members...")
                        }
                    } else {
                        // Show message if no members available
                        Text(
                            text = "No members found. Add members to the trip first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                LoadingButton(
                    text = when (viewModel.mode) {
                        is ExpenseFormMode.Add -> "Add Expense"
                        is ExpenseFormMode.Edit -> "Update Expense"
                    },
                    isLoading = uiState.isLoading,
                    onClick = viewModel::saveExpense,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

            }
        }
        SuccessToast(
            message = message,
            visible = showSuccessToast,
            onDismiss = { showSuccessToast = false }
        )

        FailureToast(
            message = uiState.amountError ?: "An unknown error occurred",
            visible = showFailureToast,
            onDismiss = { showFailureToast = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidBySelector(
    members: List<TripMember>,
    selectedMemberId: String?,
    onMemberSelected: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMember = members.find { it.id == selectedMemberId }

    Column {

        Text(
            text = "Who Paid *",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded && enabled }
        ) {
            OutlinedTextField(
                value = selectedMember?.displayName ?: "Select Member",
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.PersonAdd, null)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false}
            ) {
                members.forEach { member ->
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(member.displayName)
                                if(member.id == selectedMemberId){
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onMemberSelected(member.id)
                            expanded = false
                        }
                    )
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            TextButton(
                onClick = { showDatePicker = true },
                enabled = enabled
            ) {
                Text("Pick")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant
                                .ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onDateChange(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@Composable
fun CategorySelector(
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier,
    enabled: Boolean
) {
    Column(modifier = modifier)
    {
        Text(
            text = "Category *",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )

        //Display categories in flow layout, 2 per row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Category.entries.chunked(2).forEach { rowCategories ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowCategories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(
                                text = "${category.icon} ${category.displayName}",
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add empty space if odd number
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberSelectionList(
    members: List<TripMember>,
    selectedMemberIds: Set<String>,
    onMemberToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            members.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMemberToggle(member.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = member.id in selectedMemberIds,
                            onCheckedChange = { onMemberToggle(member.id) }
                        )
                        Text(
                            text = member.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Select All / Deselect All
            Divider()
            TextButton(
                onClick = {
                    if (selectedMemberIds.size == members.size) {
                        // Deselect all
                        members.forEach { onMemberToggle(it.id) }
                    } else {
                        // Select all
                        members.filter { it.id !in selectedMemberIds }
                            .forEach { onMemberToggle(it.id) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    if (selectedMemberIds.size == members.size) "Deselect All" else "Select All"
                )
            }
        }
    }
}
