package com.example.splitify.presentation.trips


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.CustomTextStyles
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    tripId: String?,
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved, uiState.createdTripId) {
        if (uiState.isSaved) {

            onNavigateBack()
        }
    }

    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            delay(2000)
            showCopiedMessage = false
        }
    }

    val startDateText = uiState.startDate.format(DateTimeFormatter.ofPattern("MMM, dd, yyyy"))
    val endDateText = uiState.endDate?.format(DateTimeFormatter.ofPattern("MMM, dd, yyyy")) ?: "Not Set"

    Scaffold(
        topBar = {
            SplitifyAppBar(
                title = if (viewModel.mode is CreateTripFormMode.CreateTrip) "Create Trip" else "Edit Trip",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = viewModel::saveTrip,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CustomShapes.ButtonLargeShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColors.Primary600),
                    enabled = !uiState.isLoading && uiState.name.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        val isCreate = viewModel.mode is CreateTripFormMode.CreateTrip
                        Icon(if (isCreate) Icons.Default.Add
                        else Icons.Default.Save, null)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                        if (isCreate) "Create Trip"
                                else "Save Changes",
                            style = CustomTextStyles.ButtonLarge
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // The Box contains the scrollable content and the floating "Copied" feedback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // FIX: Prevents content hiding behind Top/Bottom bars
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                TripPlanHeader()

                Spacer(modifier = Modifier.height(32.dp))

                InputFieldLabel("Trip Name")

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.TravelExplore, null, tint = PrimaryColors.Primary500) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(KeyboardCapitalization.Words),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = CustomShapes.TextFieldShape,
                    colors = premiumTextFieldColors(),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                InputFieldLabel("Description (Optional)")

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = CustomShapes.TextFieldShape,
                    colors = premiumTextFieldColors(),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumDatePickerField(
                        label = "Start Date",
                        dateText = startDateText,
                        currentDate = uiState.startDate,
                        onDateChange = viewModel::onStartDateChange,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumDatePickerField(
                        label = "End Date",
                        dateText = endDateText,
                        currentDate = uiState.endDate,
                        onDateChange = viewModel::onEndDateChange,
                        modifier = Modifier.weight(1f),
                        allowNull = true
                    )
                }

                if (uiState.dateError != null) {
                    Text(
                        text = uiState.dateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                PremiumInviteCodeCard(
                    inviteCode = uiState.inviteCode,
                    onCopyClick = {
                        clipboardManager.setText(AnnotatedString(uiState.inviteCode))
                        showCopiedMessage = true
                    },
                    onRegenerateClick = viewModel::regenerateInviteCode
                )
            }

            // Floating feedback Snackbar stays inside the Box
            AnimatedVisibility(
                visible = showCopiedMessage,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                CopyFeedbackBar()
            }
        }
    }
}


@Composable
private fun PremiumInviteCodeCard(
    inviteCode: String,
    onCopyClick: () -> Unit,
    onRegenerateClick: () -> Unit
) {
    // Shimmer animation for the card
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.CardShape,
        color = SecondaryColors.Secondary50,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = SecondaryColors.Secondary600,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Invite Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryColors.Secondary900
                    )
                }

                IconButton(
                    onClick = onRegenerateClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint = SecondaryColors.Secondary600
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Invite Code Display
            Surface(
                onClick = onCopyClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = PrimaryColors.Primary600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColors.Primary700,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = PrimaryColors.Primary100.copy(alpha = shimmerAlpha)
                    ) {
                        Text(
                            text = "TAP TO COPY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColors.Primary700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Share this code with members to join the trip",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600
            )
        }
    }
}
@Composable
private fun PremiumDatePickerField(
    label: String,
    dateText: String,
    currentDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    allowNull: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        InputFieldLabel(label)
        Surface(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.TextFieldShape,
            color = Color.White,
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(NeutralColors.Neutral300))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = PrimaryColors.Primary500, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = dateText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showDatePicker) {
        // Reuse your existing DatePickerDialog logic here
        SplitifyDatePickerDialog(
            initialDate = currentDate ?: LocalDate.now(),
            allowNull = allowNull,
            onDismiss = { showDatePicker = false },
            onDateSelected = { onDateChange(it) }
        )
    }
}

@Composable
private fun TripPlanHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = MaterialTheme.shapes.medium,
            color = PrimaryColors.Primary100,
            shadowElevation = 2.dp
        ) {
            Icon(
                imageVector = Icons.Default.Luggage,
                contentDescription = null,
                modifier = Modifier.padding(14.dp),
                tint = PrimaryColors.Primary600
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Plan Your Trip",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Set up the basics to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral600
            )
        }
    }
}

@Composable
private fun CopyFeedbackBar() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = SecondaryColors.Secondary600,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Invite code copied!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitifyDatePickerDialog(
    initialDate: LocalDate,
    allowNull: Boolean = false,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate?) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("OK", color = PrimaryColors.Primary600, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (allowNull) {
                    TextButton(onClick = {
                        onDateSelected(null)
                        onDismiss()
                    }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = PrimaryColors.Primary600,
                todayContentColor = PrimaryColors.Primary600,
                todayDateBorderColor = PrimaryColors.Primary600
            )
        )
    }
}

@Composable
fun premiumTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryColors.Primary500,
    unfocusedBorderColor = NeutralColors.Neutral300,
    focusedContainerColor = PrimaryColors.Primary50,
    unfocusedContainerColor = Color.White
)

@Composable
private fun InputFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
