//package com.example.splitify.presentation.auth
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.FocusDirection
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//
///**
// * Sign up screen
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SignUpScreen(
//    onNavigateBack: () -> Unit,
//    onSignUpSuccess: () -> Unit,
//    viewModel: SignUpViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val focusManager = LocalFocusManager.current
//
//    // Navigate on success
//    LaunchedEffect(uiState.isSignedUp) {
//        if (uiState.isSignedUp) {
//            onSignUpSuccess()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Create Account") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, "Back")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .verticalScroll(rememberScrollState())
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Welcome text
//            Text(
//                text = "Join Splitify",
//                style = MaterialTheme.typography.headlineMedium,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = "Create an account to start tracking expenses",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//
//            // Email field
//            OutlinedTextField(
//                value = uiState.email,
//                onValueChange = viewModel::onEmailChange,
//                label = { Text("Email *") },
//                placeholder = { Text("your@email.com") },
//                isError = uiState.emailError != null,
//                supportingText = uiState.emailError?.let { { Text(it) } },
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Email,
//                    imeAction = ImeAction.Next
//                ),
//                keyboardActions = KeyboardActions(
//                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
//                ),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !uiState.isLoading
//            )
//
//            // Username field
//            OutlinedTextField(
//                value = uiState.userName,
//                onValueChange = viewModel::onUserNameChange,
//                label = { Text("Username *") },
//                placeholder = { Text("john_doe") },
//                isError = uiState.userNameError != null,
//                supportingText = uiState.userNameError?.let { { Text(it) } },
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Text,
//                    imeAction = ImeAction.Next
//                ),
//                keyboardActions = KeyboardActions(
//                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
//                ),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !uiState.isLoading
//            )
//
//            // Full name field
//            OutlinedTextField(
//                value = uiState.fullName,
//                onValueChange = viewModel::onFullNameChange,
//                label = { Text("Full Name (Optional)") },
//                placeholder = { Text("John Doe") },
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Text,
//                    imeAction = ImeAction.Next
//                ),
//                keyboardActions = KeyboardActions(
//                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
//                ),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !uiState.isLoading
//            )
//
//            // Password field
//            var passwordVisible by remember { mutableStateOf(false) }
//
//            OutlinedTextField(
//                value = uiState.password,
//                onValueChange = viewModel::onPasswordChange,
//                label = { Text("Password *") },
//                placeholder = { Text("At least 6 characters") },
//                isError = uiState.passwordError != null,
//                supportingText = uiState.passwordError?.let { { Text(it) } },
//                visualTransformation = if (passwordVisible)
//                    VisualTransformation.None
//                else
//                    PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                        Icon(
//                            imageVector = if (passwordVisible)
//                                Icons.Default.VisibilityOff
//                            else
//                                Icons.Default.Visibility,
//                            contentDescription = if (passwordVisible)
//                                "Hide password"
//                            else
//                                "Show password"
//                        )
//                    }
//                },
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Password,
//                    imeAction = ImeAction.Next
//                ),
//                keyboardActions = KeyboardActions(
//                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
//                ),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !uiState.isLoading
//            )
//
//            // Confirm password field
//            var confirmPasswordVisible by remember { mutableStateOf(false) }
//
//            OutlinedTextField(
//                value = uiState.confirmPassword,
//                onValueChange = viewModel::onConfirmPasswordChange,
//                label = { Text("Confirm Password *") },
//                placeholder = { Text("Re-enter password") },
//                isError = uiState.confirmPasswordError != null,
//                supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
//                visualTransformation = if (confirmPasswordVisible)
//                    VisualTransformation.None
//                else
//                    PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
//                        Icon(
//                            imageVector = if (confirmPasswordVisible)
//                                Icons.Default.VisibilityOff
//                            else
//                                Icons.Default.Visibility,
//                            contentDescription = if (confirmPasswordVisible)
//                                "Hide password"
//                            else
//                                "Show password"
//                        )
//                    }
//                },
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Password,
//                    imeAction = ImeAction.Done
//                ),
//                keyboardActions = KeyboardActions(
//                    onDone = {
//                        focusManager.clearFocus()
//                        viewModel.signUp()
//                    }
//                ),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !uiState.isLoading
//            )
//
//            // Error message
//            if (uiState.errorMessage != null) {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer
//                    )
//                ) {
//                    Text(
//                        text = uiState.errorMessage!!,
//                        color = MaterialTheme.colorScheme.onErrorContainer,
//                        modifier = Modifier.padding(16.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Sign up button
//            Button(
//                onClick = viewModel::signUp,
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !uiState.isLoading
//            ) {
//                if (uiState.isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(20.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Creating account...")
//                } else {
//                    Text("Sign Up")
//                }
//            }
//
//            // Login link
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Already have an account?",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                TextButton(onClick = onNavigateBack) {
//                    Text("Login")
//                }
//            }
//        }
//    }
//}
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
                        shape = CustomShapes.ButtonLargeShape
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

            Spacer(Modifier.height(16.dp))

            SocialSignUpButtons(
                enabled = !uiState.isLoading,
                onGoogleClick = onGoogleSignUp,
            )

            Spacer(Modifier.height(24.dp))

            LoginLink(onNavigateBack)
        }
    }
}

@Composable
private fun SocialSignUpButtons(
    onGoogleClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onGoogleClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = CustomShapes.ButtonLargeShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color.White.copy(alpha = 0.5f))
        ),
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.Default.Login,
            contentDescription = null,
            tint = NeutralColors.Neutral700
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Sign Up with Google",
            style = CustomTextStyles.ButtonMedium,
            color = NeutralColors.Neutral900
        )
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
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Join thousands splitting expenses",
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onDone?.invoke() }
        ),
        enabled = enabled
    )
}
