package com.example.splitify.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.theme.*

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onGoogleSignUp: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSignedUp) {
        if (uiState.isSignedUp)
            onSignUpSuccess()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GradientColors.SuccessGradientStart,
                        GradientColors.SuccessGradientEnd
                    )
                )
            )
    ) {

        DecorativeCircles(rotation)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            SignUpHeader()

            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CustomShapes.CardElevatedShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(24.dp)) {

                    Text(
                        "Create your account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthTextField(
                        value = uiState.fullName,
                        onChange = viewModel::onFullNameChange,
                        label = "Full Name(Optional)",
                        icon = Icons.Default.Person,
                        enabled = !uiState.isLoading,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    AuthTextField(
                        value = uiState.email,
                        onChange = viewModel::onEmailChange,
                        label = "Email",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        error = uiState.emailError,
                        enabled = !uiState.isLoading,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    AuthTextField(
                        value = uiState.userName,
                        onChange = viewModel::onUserNameChange,
                        label = "Username",
                        icon = Icons.Default.AccountCircle,
                        error = uiState.userNameError,
                        enabled = !uiState.isLoading,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    PasswordField(
                        value = uiState.password,
                        onChange = viewModel::onPasswordChange,
                        label = "Password",
                        error = uiState.passwordError,
                        enabled = !uiState.isLoading,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    PasswordField(
                        value = uiState.confirmPassword,
                        onChange = viewModel::onConfirmPasswordChange,
                        label = "Confirm Password",
                        error = uiState.confirmPasswordError,
                        enabled = !uiState.isLoading,
                        imeAction = ImeAction.Done,
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.signUp()
                        }
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.signUp() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CustomShapes.ButtonLargeShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColors.Primary600
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Create Account", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            //DividerText("OR")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(Modifier.height(24.dp))

            LoginLink(onNavigateBack)
        }
    }
}

@Composable
private fun LoginLink(
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Already have an account? ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = "Sign In",
            modifier = Modifier
                .clickable { onClick() }
                .padding(4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SignUpHeader() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                shadowElevation = 8.dp
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Sign Up",
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize(),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Splitify",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Share expenses, not Friendships",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}


@Composable
private fun AuthTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    enabled: Boolean,
    onNext: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = CustomShapes.TextFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColors.Primary500,
            unfocusedBorderColor = NeutralColors.Neutral300,
            errorBorderColor = SemanticColors.Error
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        enabled = enabled
    )
}

@Composable
private fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    imeAction: ImeAction = ImeAction.Next,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    null
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = CustomShapes.TextFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColors.Primary500,
            unfocusedBorderColor = NeutralColors.Neutral300,
            errorBorderColor = SemanticColors.Error
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onDone?.invoke() }
        ),
        enabled = enabled
    )
}
