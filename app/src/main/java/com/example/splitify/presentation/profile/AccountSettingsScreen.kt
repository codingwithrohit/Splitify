package com.example.splitify.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onLogOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val deleteAccountState by viewModel.deleteAccountState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
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
                        GoogleSignIn.getClient(
                            context,
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        ).signOut().addOnCompleteListener {
                            viewModel.deleteAccount(onLogOut)
                        }
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

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = null
                viewModel.resetDeleteAccountState()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PrimaryColors.Primary600,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; passwordError = null },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CustomShapes.TextFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColors.Primary500,
                            unfocusedBorderColor = NeutralColors.Neutral300
                        )
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordError = null },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CustomShapes.TextFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColors.Primary500,
                            unfocusedBorderColor = NeutralColors.Neutral300
                        )
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordError = null },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = CustomShapes.TextFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColors.Primary500,
                            unfocusedBorderColor = NeutralColors.Neutral300
                        )
                    )

                    passwordError?.let {
                        Text(it, color = SemanticColors.Error, style = MaterialTheme.typography.bodySmall)
                    }

                    if (deleteAccountState is DeleteAccountState.Error) {
                        Text(
                            (deleteAccountState as DeleteAccountState.Error).message,
                            color = SemanticColors.Error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            currentPassword.isBlank() -> passwordError = "Enter current password"
                            newPassword.length < 6 -> passwordError = "New password must be at least 6 characters"
                            newPassword != confirmPassword -> passwordError = "Passwords do not match"
                            else -> viewModel.changePassword(currentPassword, newPassword) {
                                showChangePasswordDialog = false
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        }
                    },
                    enabled = deleteAccountState !is DeleteAccountState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColors.Primary600),
                    shape = CustomShapes.ButtonShape
                ) {
                    if (deleteAccountState is DeleteAccountState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Update Password", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    passwordError = null
                    viewModel.resetDeleteAccountState()
                }) {
                    Text("Cancel", color = NeutralColors.Neutral600)
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