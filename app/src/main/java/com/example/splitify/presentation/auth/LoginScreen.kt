//package com.example.splitify.presentation.auth
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
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
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LoginScreen(
//    onNavigateToSignUp: () -> Unit,
//    onLoginSuccess: () -> Unit,
//    viewModel: LoginViewModel = hiltViewModel()
//){
//    val uiState by viewModel.uiState.collectAsState()
//    val focusManager = LocalFocusManager.current
//
//    LaunchedEffect(uiState.isLoggedIn) {
//        if(uiState.isLoggedIn){
//            onLoginSuccess()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title =  { Text("Welcome Back") }
//            )
//        }
//    ) {paddingValues ->
//
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//                .padding(24.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//
//        ) {
//
//            Text(
//                text = "Splitify",
//                style = MaterialTheme.typography.displayMedium,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Text(
//                text = "Split expenses, not friendships",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier.padding(bottom = 48.dp)
//            )
//
//            OutlinedTextField(
//                value = uiState.email,
//                onValueChange = viewModel::onEmailChange,
//                label = {Text("Email")},
//                placeholder = {Text("Enter your email address")},
//                isError = uiState.emailError != null,
//                supportingText = uiState.emailError?.let { {Text(it)} },
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
//            var passwordVisible by remember { mutableStateOf(false) }
//
//            OutlinedTextField(
//                value = uiState.password,
//                onValueChange = viewModel::onPasswordChange,
//                label = { Text("Password") },
//                placeholder = { Text("*********") },
//                isError = uiState.passwordError != null,
//                supportingText = uiState.passwordError?.let { {Text(it)} },
//                visualTransformation = if(passwordVisible){
//                    VisualTransformation.None
//                }else{
//                    PasswordVisualTransformation()
//                },
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Password,
//                    imeAction = ImeAction.Done
//                ),
//                keyboardActions = KeyboardActions(
//                    onDone = {
//                        focusManager.clearFocus()
//                        viewModel.login()
//                    }
//                ),
//                trailingIcon = {
//                    IconButton(
//                        onClick = { passwordVisible = !passwordVisible })
//                    {
//                        Icon(
//                            imageVector = if (passwordVisible)
//                                Icons.Default.VisibilityOff
//                            else
//                                Icons.Default.Visibility,
//                            contentDescription = if (passwordVisible)
//                                "Hide Password"
//                            else
//                                "Show Password"
//                        )
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                enabled = !uiState.isLoading
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
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
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            //Button
//            Button(
//                onClick = viewModel::login,
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !uiState.isLoading
//            ) {
//                if (uiState.isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(20.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Logging in...")
//                } else {
//                    Text("Login")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sign up link
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Don't have an account?",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                TextButton(onClick = onNavigateToSignUp) {
//                    Text("Sign Up")
//                }
//            }
//
//        }
//
//    }
//
//}
package com.example.splitify.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.theme.*

/**
 * Premium Login Screen with:
 * - Gradient background with animated decorative elements
 * - Smooth animations
 * - Modern glassmorphism-style card
 * - Social login buttons
 * - Smooth error states
 *
 * Free SVG illustrations from:
 * - unDraw: https://undraw.co (search "finance" or "mobile")
 * - Storyset: https://storyset.com (animated illustrations)
 * - IRA Design: https://iradesign.io (gradient illustrations)
 */

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isLoggedIn) {
        if(uiState.isLoggedIn){
            onLoginSuccess()
        }
    }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

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
        // Animated decorative circles
        DecorativeCircles(rotation = rotation)

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo and title
            AppLogo()

            Spacer(modifier = Modifier.height(48.dp))

            // Login card
            LoginCard(
                uiState = uiState,
                passwordVisible = passwordVisible,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                onLoginClick = {
                    focusManager.clearFocus()
                    viewModel.login()
                },
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with text
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

            Spacer(modifier = Modifier.height(24.dp))

            // Social login buttons
            SocialLoginButtons(
                onGoogleClick = {},
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign up link
            SignUpLink(onClick = onNavigateToSignUp)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DecorativeCircles(rotation: Float) {
    // Top right circle
    Box(
        modifier = Modifier
            .offset(x = 150.dp, y = (-50).dp)
            .size(200.dp)
            .rotate(rotation)
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape
            )
    )

    // Bottom left circle
    Box(
        modifier = Modifier
            .offset(x = (-100).dp, y = 500.dp)
            .size(300.dp)
            .rotate(-rotation)
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = CircleShape
            )
    )
}

@Composable
private fun AppLogo() {
    // Animated logo appearance
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
            // Logo icon (you can replace with custom SVG)
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                shadowElevation = 8.dp
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Splitify Logo",
                    modifier = Modifier
                        .padding(20.dp)
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
private fun LoginCard(
    uiState: LoginUiState,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    focusManager: FocusManager
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.CardElevatedShape,
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900
            )

            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral600
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
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
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = SemanticColors.ErrorDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { {Text(it)} },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = CustomShapes.TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColors.Primary500,
                    unfocusedBorderColor = NeutralColors.Neutral300
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                singleLine = true,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { {Text(it)} },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLoginClick()
                    }
                ),
                shape = CustomShapes.TextFieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColors.Primary500,
                    unfocusedBorderColor = NeutralColors.Neutral300
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot password
            Text(
                text = "Forgot Password?",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(enabled = !uiState.isLoading) { { /* Handle forgot password */ } }
                    .padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryColors.Primary600,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CustomShapes.ButtonLargeShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColors.Primary600
                ),
                enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = CustomTextStyles.ButtonLarge,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLoginButtons(
    onGoogleClick: () -> Unit,
    enabled: Boolean
) {
    // Google Sign In button
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
        // Google icon (use actual Google icon in production)
        Icon(
            imageVector = Icons.Default.Login,
            contentDescription = null,
            tint = NeutralColors.Neutral700
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Continue with Google",
            style = CustomTextStyles.ButtonMedium,
            color = NeutralColors.Neutral900
        )
    }
}

@Composable
private fun SignUpLink(onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Don't have an account? ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = "Sign Up",
            modifier = Modifier
                .clickable { onClick() }
                .padding(4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SplitifyTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToSignUp = {},
        )
    }
}