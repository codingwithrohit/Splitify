package com.example.splitify.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors

@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onLogOut: () -> Unit,
    viewModel: ProfileViewModel
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val deleteAccountState by viewModel.deleteAccountState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SplitifyAppBar(
                title = "Account Settings",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColors.Neutral50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Information Card
            ProfileSection(title = "Profile Information") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow("Name", userProfile.name)
                    InfoRow("Email", userProfile.email)
                    InfoRow("User ID", userProfile.userId)
                }
            }

            // Security Section
            ProfileSection(title = "Security") {
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    subtitle = "Update your password",
                    onClick = { showChangePasswordDialog = true },
                    showChevron = false
                )
            }

            // Danger Zone
            ProfileSection(title = "Danger Zone") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Delete Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SemanticColors.Error
                    )

                    Text(
                        text = "Once you delete your account, there is no going back. All your trips, expenses, and data will be permanently deleted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral700
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SemanticColors.Error
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = CustomShapes.ButtonShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Delete My Account",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = SemanticColors.Error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This action cannot be undone. All your data will be permanently deleted:",
                        color = NeutralColors.Neutral700
                    )
                    Text("• All trips and expenses", color = NeutralColors.Neutral600)
                    Text("• All payment history", color = NeutralColors.Neutral600)
                    Text("• Your account information", color = NeutralColors.Neutral600)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Are you absolutely sure?",
                        fontWeight = FontWeight.Bold,
                        color = SemanticColors.Error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(onLogOut)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticColors.Error
                    ),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }

    // Change Password Dialog (placeholder)
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PrimaryColors.Primary600,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Change Password",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Password change functionality will be available soon. You can reset your password from the login screen.",
                    color = NeutralColors.Neutral700
                )
            },
            confirmButton = {
                Button(
                    onClick = { showChangePasswordDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColors.Primary600
                    ),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = NeutralColors.Neutral600,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralColors.Neutral900
        )
    }
}