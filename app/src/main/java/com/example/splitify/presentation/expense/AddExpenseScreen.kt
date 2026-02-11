package com.example.splitify.presentation.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.splitify.domain.model.Category
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.components.InlineErrorMessage
import com.example.splitify.presentation.theme.CategoryColors
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.CustomTextStyles
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import com.example.splitify.util.CurrencyUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigationBack: () -> Unit,
    navController: NavController,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isGroupExpense by viewModel.isGroupExpense.collectAsStateWithLifecycle()
    val selectedMemberIds by viewModel.selectedMemberIds.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            val targetTab = if (isGroupExpense) 0 else 1
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("target_tab", targetTab)

            onNavigationBack()
        }
    }
//    LaunchedEffect(isGroupExpense) {
//        if (isGroupExpense) {
//            scrollState.animateScrollTo(scrollState.maxValue)
//        }
//    }
    // 1. Keep track of whether we just toggled the group mode
    var shouldScrollToBottom by remember { mutableStateOf(false) }

    LaunchedEffect(isGroupExpense) {
        if (isGroupExpense) {
            shouldScrollToBottom = true
        }
    }

// 2. This Effect triggers whenever the scroll range increases (maxValue changes)
// but only if we actually intend to scroll.
    LaunchedEffect(scrollState.maxValue) {
        if (shouldScrollToBottom) {
            scrollState.animateScrollTo(scrollState.maxValue)
            shouldScrollToBottom = false // Reset after scrolling
        }
    }


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        Text(
                            text = when (viewModel.mode) {
                                is ExpenseFormMode.Add -> "Add Expense"
                                is ExpenseFormMode.Edit -> "Edit Expense"
                            },
                            fontWeight = FontWeight.Bold,
                            color = NeutralColors.Neutral900
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigationBack,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeutralColors.Neutral100)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = NeutralColors.Neutral700
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
    ) { paddingValues ->

        if (uiState.isLoading && uiState.members.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryColors.Primary500
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Amount input card with highlighted background
                    AmountInputCard(
                        amount = uiState.amount,
                        onAmountChange = viewModel::onAmountChange,
                        hasError = uiState.amountError != null,
                        errorMessage = uiState.amountError,
                        enabled = !uiState.isLoading
                    )

                    // Description field
                    Column {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("What was this expense for?") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = PrimaryColors.Primary500
                                )
                            },
                            isError = uiState.descriptionError != null,
                            supportingText = {
                                if (uiState.descriptionError != null) {
                                    uiState.descriptionError?.let { InlineErrorMessage(it) }
                                } else {
                                    Text(
                                        text = "${uiState.description.length}/50 characters",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralColors.Neutral500
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            shape = CustomShapes.TextFieldShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColors.Primary500,
                                unfocusedBorderColor = NeutralColors.Neutral300
                            ),
                            enabled = !uiState.isLoading
                        )
                    }

                    // Category selector
                    CategorySelector(
                        selectedCategory = uiState.category,
                        onCategorySelected = viewModel::onCategoryChange,
                        enabled = !uiState.isLoading
                    )

                    // Date picker
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        DatePickerField(
                            date = uiState.expenseDate,
                            onDateChange = viewModel::onDateChange,
                            enabled = !uiState.isLoading
                        )
                    }

                    // Who paid selector
                    PaidBySelector(
                        members = uiState.members,
                        selectedMemberId = uiState.paidByMemberId,
                        onMemberSelected = viewModel::onPaidByChange,
                        enabled = !uiState.isLoading
                    )

                    // Split type toggle
                    Column {
                        Text(
                            text = "Split Type",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SplitTypeToggle(
                            isGroupExpense = isGroupExpense,
                            onSplitTypeChange = viewModel::setIsGroupExpense,
                            enabled = !uiState.isLoading
                        )
                    }

                    // Member selection (only for group expenses)
                    AnimatedVisibility(
                        visible = isGroupExpense,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Split With",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                if (uiState.members.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            if (selectedMemberIds.size == uiState.members.size) {
                                                uiState.members.forEach { viewModel.toggleMember(it.id) }
                                            } else {
                                                uiState.members
                                                    .filter { it.id !in selectedMemberIds }
                                                    .forEach { viewModel.toggleMember(it.id) }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = if (selectedMemberIds.size == uiState.members.size)
                                                "Deselect All"
                                            else
                                                "Select All",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PrimaryColors.Primary600
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.members.isNotEmpty()) {
                                MemberSelectionList(
                                    members = uiState.members,
                                    selectedMemberIds = selectedMemberIds,
                                    onMemberToggle = viewModel::toggleMember
                                )

                                // Split preview card
                                if (selectedMemberIds.isNotEmpty() && uiState.amount.isNotBlank()) {
                                    val amount = uiState.amount.toDoubleOrNull() ?: 0.0
                                    val splitAmount = amount / selectedMemberIds.size

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CustomShapes.CardShape,
                                        color = SecondaryColors.Secondary50
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Each person owes",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SecondaryColors.Secondary700
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${selectedMemberIds.size} members",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = NeutralColors.Neutral600
                                                )
                                            }
                                            Text(
                                                text = CurrencyUtils.format(splitAmount),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryColors.Secondary600
                                            )
                                        }
                                    }
                                }
                            } else if (uiState.isLoading) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryColors.Primary500
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Loading members...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NeutralColors.Neutral600
                                    )
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = CustomShapes.CardShape,
                                    color = NeutralColors.Neutral100
                                ) {
                                    Text(
                                        text = "No members found. Add members to the trip first.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NeutralColors.Neutral600,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating save button
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(20.dp),
                    shape = CustomShapes.ButtonLargeShape,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = viewModel::saveExpense,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CustomShapes.ButtonLargeShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryColors.Secondary600
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = when (viewModel.mode) {
                                    is ExpenseFormMode.Add -> "Add Expense"
                                    is ExpenseFormMode.Edit -> "Update Expense"
                                },
                                style = CustomTextStyles.ButtonLarge
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun SplitTypeToggle(
    isGroupExpense: Boolean,
    onSplitTypeChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SplitTypeButton(
            text = "Personal",
            icon = Icons.Default.Person,
            isSelected = !isGroupExpense,
            onClick = { onSplitTypeChange(false) },
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )

        SplitTypeButton(
            text = "Group",
            icon = Icons.Default.Groups,
            isSelected = isGroupExpense,
            onClick = { onSplitTypeChange(true) },
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
private fun SplitTypeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = CustomShapes.ButtonShape,
        color = if (isSelected) PrimaryColors.Primary500 else Color.White,
        border = if (!isSelected)
            androidx.compose.foundation.BorderStroke(1.dp, NeutralColors.Neutral300)
        else null,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else NeutralColors.Neutral600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else NeutralColors.Neutral700
            )
        }
    }
}

@Composable
private fun MemberSelectionList(
    members: List<TripMember>,
    selectedMemberIds: Set<String>,
    onMemberToggle: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.CardShape,
        color = NeutralColors.Neutral50
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            members.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMemberToggle(member.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = member.id in selectedMemberIds,
                        onCheckedChange = { onMemberToggle(member.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryColors.Primary600
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NeutralColors.Neutral600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral900
                    )
                }

                if (member != members.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = NeutralColors.Neutral200
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountInputCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    hasError: Boolean,
    errorMessage: String?,
    enabled: Boolean
) {
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.CardShape,
            color = if (hasError)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else
                PrimaryColors.Primary50
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hasError)
                        MaterialTheme.colorScheme.error
                    else
                        PrimaryColors.Primary700,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasError)
                            MaterialTheme.colorScheme.error
                        else
                            PrimaryColors.Primary700
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    BasicTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (hasError)
                                MaterialTheme.colorScheme.error
                            else
                                PrimaryColors.Primary700,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.wrapContentWidth()
                            .background(PrimaryColors.Primary500.copy(alpha = 0.3f)),
                        singleLine = true,
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryColors.Primary300,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
//                CenteredAmountInput(
//                    amount = amount,
//                    onAmountChange = onAmountChange,
//                    hasError = hasError,
//                    enabled = enabled
//                )
            }
        }

        if (hasError && errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Category.entries.chunked(2).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCategories.forEach { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (category) {
        Category.FOOD -> CategoryColors.Food
        Category.TRANSPORT -> CategoryColors.Transport
        Category.ACCOMMODATION -> CategoryColors.Accommodation
        Category.ENTERTAINMENT -> CategoryColors.Entertainment
        Category.SHOPPING -> CategoryColors.Shopping
        Category.OTHER -> CategoryColors.Other
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected)
            categoryColor.copy(alpha = 0.15f)
        else
            Color.White,
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, categoryColor)
        else
            androidx.compose.foundation.BorderStroke(1.dp, NeutralColors.Neutral300),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = category.icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) categoryColor else NeutralColors.Neutral700,
                maxLines = 1,
                softWrap = false,
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    enabled: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Surface(
        onClick = { if (enabled) showDatePicker = true },
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.TextFieldShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = NeutralColors.Neutral300
        ),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = PrimaryColors.Primary500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Pick",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryColors.Primary600,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaidBySelector(
    members: List<TripMember>,
    selectedMemberId: String?,
    onMemberSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMember = members.find { it.id == selectedMemberId }

    Column {
        Text(
            text = "Who Paid",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded && enabled }
        ) {
            OutlinedTextField(
                value = selectedMember?.displayName ?: "Select member",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryColors.Primary500
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                shape = CustomShapes.TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColors.Primary500,
                    unfocusedBorderColor = NeutralColors.Neutral300
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                members.forEach { member ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.displayName)
                                if (member.id == selectedMemberId) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryColors.Primary600,
                                        modifier = Modifier.size(18.dp)
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

@Composable
private fun CenteredAmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    hasError: Boolean,
    enabled: Boolean
) {
    SubcomposeLayout { constraints ->

        val rupeePlaceable = subcompose("rupee") {
            Text(
                text = "₹",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (hasError)
                    MaterialTheme.colorScheme.error
                else
                    PrimaryColors.Primary700
            )
        }[0].measure(constraints)

        val textFieldPlaceable = subcompose("textfield") {
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (hasError)
                        MaterialTheme.colorScheme.error
                    else
                        PrimaryColors.Primary700,
                    textAlign = TextAlign.Start
                ),
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                decorationBox = { inner ->
                    if (amount.isEmpty()) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColors.Primary300
                        )
                    }
                    inner()
                }
            )
        }[0].measure(constraints)

        val totalWidth = rupeePlaceable.width + 4.dp.roundToPx() + textFieldPlaceable.width
        val startX = (constraints.maxWidth - totalWidth) / 2

        layout(
            width = constraints.maxWidth,
            height = maxOf(rupeePlaceable.height, textFieldPlaceable.height)
        ) {
            var x = startX
            rupeePlaceable.placeRelative(x, 0)
            x += rupeePlaceable.width + 4.dp.roundToPx()
            textFieldPlaceable.placeRelative(x, 0)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Card(){
    AmountInputCard(
        amount = "",
        onAmountChange = {},
        hasError = false,
        errorMessage = null,
        enabled = true
    )
}

//@Composable
//private fun AmountInputCard(
//    amount: String,
//    onAmountChange: (String) -> Unit,
//    hasError: Boolean,
//    errorMessage: String?,
//    enabled: Boolean
//)