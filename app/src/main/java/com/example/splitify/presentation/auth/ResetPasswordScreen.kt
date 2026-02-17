package com.example.splitify.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.CustomTextStyles
import com.example.splitify.presentation.theme.GradientColors
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors

@Composable
fun ResetPasswordScreen(
    deepLinkUri: android.net.Uri? = null,
    onResetSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { viewModel.initializeResetSession(it) }
    }
    LaunchedEffect(uiState.passwordUpdateSuccess) {
        if (uiState.passwordUpdateSuccess) {
            onResetSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientColors.PrimaryGradientStart,
                        GradientColors.PrimaryGradientEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Set New Password",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Choose a strong password",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CustomShapes.CardElevatedShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Error display
                    val displayError = localError ?: uiState.errorMessage
                    AnimatedVisibility(
                        visible = displayError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CustomShapes.CardShape,
                            color = SemanticColors.ErrorLight
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = SemanticColors.Error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SemanticColors.ErrorDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Success state
                    if (uiState.passwordUpdateSuccess) {
                        Text(
                            text = "✅ Password updated!",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryColors.Primary600,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can now sign in with your new password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralColors.Neutral600
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onResetSuccess,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CustomShapes.ButtonLargeShape,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColors.Primary600)
                        ) {
                            Text("Go to Login", style = CustomTextStyles.ButtonLarge, fontSize = 16.sp)
                        }
                        return@Column
                    }

                    // New password field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            localError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        shape = CustomShapes.TextFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColors.Primary500,
                            unfocusedBorderColor = NeutralColors.Neutral300
                        ),
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            localError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (newPassword == confirmPassword && newPassword.length >= 6) {
                                    viewModel.updatePassword(newPassword)
                                } else {
                                    localError = if (newPassword != confirmPassword)
                                        "Passwords do not match"
                                    else
                                        "Password must be at least 6 characters"
                                }
                            }
                        ),
                        shape = CustomShapes.TextFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColors.Primary500,
                            unfocusedBorderColor = NeutralColors.Neutral300
                        ),
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (newPassword != confirmPassword) {
                                localError = "Passwords do not match"
                            } else if (newPassword.length < 6) {
                                localError = "Password must be at least 6 characters"
                            } else {
                                localError = null
                                viewModel.updatePassword(newPassword)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CustomShapes.ButtonLargeShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColors.Primary600),
                        enabled = !uiState.isLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update Password", style = CustomTextStyles.ButtonLarge, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}