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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.runtime.DisposableEffect
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
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
            viewModel.resetPasswordResetState()
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
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "No worries, we'll send you a reset link",
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

                    if (!uiState.passwordResetEmailSent) {

                        AnimatedVisibility(
                            visible = uiState.errorMessage != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut()
                        ) {
                            Column {
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
                                            text = uiState.errorMessage ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SemanticColors.ErrorDark
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Text(
                            text = "Enter your email address",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralColors.Neutral900
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModel.clearError()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.sendPasswordResetEmail(email)
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
                                viewModel.sendPasswordResetEmail(email)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CustomShapes.ButtonLargeShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColors.Primary600
                            ),
                            enabled = !uiState.isLoading && email.isNotBlank()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Send Reset Link",
                                    style = CustomTextStyles.ButtonLarge,
                                    fontSize = 16.sp
                                )
                            }
                        }

                    } else {
                        Text(
                            text = "✅ Email Sent!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColors.Primary600
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "We sent a reset link to $email\nCheck your inbox and follow the instructions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralColors.Neutral600
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CustomShapes.ButtonLargeShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColors.Primary600
                            )
                        ) {
                            Text(
                                text = "Back to Login",
                                style = CustomTextStyles.ButtonLarge,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}