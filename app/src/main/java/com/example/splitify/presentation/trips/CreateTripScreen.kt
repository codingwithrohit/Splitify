package com.example.splitify.presentation.trips


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.components.LoadingButton
import com.example.splitify.presentation.components.SuccessToast
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var showSuccessToast by remember { mutableStateOf(false) }
    var showCopyToast by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if(uiState.isSaved){
            showSuccessToast = true
            delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Trip") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Button")
                    }
                }
            )
        }
    ) {paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
            )
        {
            //Trip Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Trip Name *") },
                placeholder = { Text("e.g. Goa Trip 2025") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            //Trip Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description(Optional)") },
                placeholder = { Text("Add details about your trip..") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            //Start Date
            DatePickerField(
                label = "Start Date *",
                date = uiState.startDate,
                onDateChange = viewModel::onStartDateChange,
                modifier = Modifier.fillMaxWidth()
            )

            //End Date
            DatePickerField(
                label = "End Date",
                date = uiState.endDate,
                onDateChange = viewModel::onEndDateChange,
                modifier = Modifier.fillMaxWidth(),
                allowNull = true
            )

            // Date error
            if (uiState.dateError != null) {
                Text(
                    text = uiState.dateError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            //Invite Code
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                Text(
//                    text = "Invite Members",
//                    style = MaterialTheme.typography.titleMedium
//                )
//
//                // The Chip
//                AssistChip(
//                    onClick = {
//                        clipboardManager.setText(AnnotatedString(uiState.inviteCode))
//                        showSuccessToast = true // Using the toast we built earlier!
//                    },
//                    label = {
//                        Text(
//                            text = "Code: ${uiState.inviteCode}",
//                            style = MaterialTheme.typography.labelLarge
//                        )
//                    },
//                    leadingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.ContentCopy,
//                            contentDescription = "Copy code",
//                            modifier = Modifier.size(18.dp)
//                        )
//                    },
//                    trailingIcon = {
//                        // We can still allow regenerating by clicking the end of the chip
//                        IconButton(
//                            onClick = viewModel::regenerateInviteCode,
//                            modifier = Modifier.size(24.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Refresh,
//                                contentDescription = "Refresh",
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//                    },
//                    shape = RoundedCornerShape(12.dp) // Keeps it consistent with your UI
//                )
//
//                Text(
//                    text = "Share this code with members to join the trip",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text( text = "Invite Code",
                        style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
//                        Text(
//                            text = uiState.inviteCode,
//                            style = MaterialTheme.typography.headlineMedium,
//                            color = MaterialTheme.colorScheme.primary
//                        )
                        AssistChip(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.inviteCode))
                        showSuccessToast = true // Using the toast we built earlier!
                    },
                    label = {
                        Text(
                            text = "Code: ${uiState.inviteCode}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp) // Keeps it consistent with your UI
                )
                        IconButton( onClick = viewModel::regenerateInviteCode ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Button")
                        }
                    }
                    Text(
                        text = "Share this code with members to join the trip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                text = "Create Trip",
                onClick = viewModel::saveTrip,
                isLoading = uiState.isLoading,
                icon = Icons.Default.Save,
                modifier = Modifier.fillMaxWidth()
            )


        }

        SuccessToast(
            message = "Trip created! 🎉",
            visible = showSuccessToast,
            onDismiss = { showSuccessToast = false }
        )
        SuccessToast(
            message = "Code copied! 📋",
            visible = showCopyToast,
            onDismiss = { showCopyToast = false }
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    allowNull: Boolean = false
    )
{
    var showDatePicker by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = date?.format(DateTimeFormatter.ofPattern("MMM,dd,yyyy")) ?: "Not Set",
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            TextButton( onClick = { showDatePicker = true } ) {
                Text("Pick")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now())
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
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
                if (allowNull) {
                    TextButton(onClick = {
                        onDateChange(null)
                        showDatePicker = false
                    }) {
                        Text("Clear")
                    }
                }
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
