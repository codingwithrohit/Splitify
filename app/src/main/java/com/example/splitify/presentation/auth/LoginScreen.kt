package com.example.splitify.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.R
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.CustomTextStyles
import com.example.splitify.presentation.theme.GradientColors
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors
import com.example.splitify.presentation.theme.SplitifyTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val webClientId = stringResource(id = R.string.google_web_client_id)

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    // 🔥 FIXED: Google Sign-In Launcher with Error Handling
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                } ?: run {
                    viewModel.showError("Failed to get ID token from Google")
                }
            } catch (e: ApiException) {
                val errorMsg = when (e.statusCode) {
                    12501 -> "Sign-in cancelled"
                    12500 -> "Sign-in failed. Please try again."
                    else -> "Google sign-in error: ${e.statusCode}"
                }
                viewModel.showError(errorMsg)
            }
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            viewModel.showError("Sign-in failed. Please try again.")
        }
    }


    LaunchedEffect(uiState.isLoggedIn) {
        if(uiState.isLoggedIn){
            onLoginSuccess()
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
            // Auto-clear after 3 seconds
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
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
                onNavigateToForgotPassword = onNavigateToForgotPassword,
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
                onGoogleClick = {googleSignInLauncher.launch(googleSignInClient.signInIntent)},
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
fun DecorativeCircles(rotation: Float) {
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
    onNavigateToForgotPassword: () -> Unit,
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
                    .clickable(enabled = !uiState.isLoading) { onNavigateToForgotPassword() }
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