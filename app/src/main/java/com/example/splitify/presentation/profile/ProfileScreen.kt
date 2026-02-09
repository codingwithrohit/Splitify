package com.example.splitify.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.splitify.data.local.SessionManager
import com.example.splitify.presentation.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    onNavigateToAccountSettings: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onLogOut: () -> Unit,
    sessionManager: SessionManager,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val profilePictureState by viewModel.profilePictureState.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Convert URI to File and upload
            val file = uriToFile(context, it)
            if (file != null) {
                viewModel.uploadProfilePicture(file)
            }
        }
    }

    // Show success/error messages
    LaunchedEffect(profilePictureState) {
        when (profilePictureState) {
            is ProfilePictureState.Success -> {
                // You can show a snackbar here
                viewModel.resetProfilePictureState()
            }
            is ProfilePictureState.Error -> {
                // Show error snackbar
                viewModel.resetProfilePictureState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColors.Neutral50)
    ) {
        // Header with gradient and profile picture
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryColors.Primary500,
                                PrimaryColors.Primary700
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Avatar with upload option
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        // Avatar
                        if (!userProfile.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(userProfile.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { showImageOptions = true },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .border(
                                        width = 3.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { showImageOptions = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(60.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        // Edit button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryColors.Primary600)
                                .border(
                                    width = 2.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { showImageOptions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    // Upload progress indicator
                    if (profilePictureState is ProfilePictureState.Uploading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        // Profile sections
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            ProfileSection(title = "Account") {
                ProfileMenuItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Account Settings",
                    subtitle = "Manage your account and security",
                    onClick = onNavigateToAccountSettings
                )
            }

            // App Settings Section
            ProfileSection(title = "App Settings") {
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Preferences",
                    subtitle = "Notifications, currency, and theme",
                    onClick = onNavigateToAppSettings
                )
            }

            // About Section
            ProfileSection(title = "About") {
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About Splitify",
                    subtitle = "Version, privacy, and terms",
                    onClick = onNavigateToAbout
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = NeutralColors.Neutral200
                )

                ProfileMenuItem(
                    icon = Icons.Default.Code,
                    title = "About Developer",
                    subtitle = "Meet the creator",
                    onClick = onNavigateToDeveloper
                )
            }

            // Logout Button
            ProfileSection(title = "") {
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    onClick = { showLogoutDialog = true },
                    iconTint = SemanticColors.Warning,
                    showChevron = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Image options bottom sheet
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = PrimaryColors.Primary600
                )
            },
            title = {
                Text("Profile Picture", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showImageOptions = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload new picture")
                    }

                    if (!userProfile.avatarUrl.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                showImageOptions = false
                                viewModel.deleteProfilePicture()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = SemanticColors.Error
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Remove picture", color = SemanticColors.Error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = SemanticColors.Warning,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout?",
                    color = NeutralColors.Neutral600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogOut)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticColors.Warning
                    ),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }
}

// Helper function to convert URI to File
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        file
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral700,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            content()
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = PrimaryColors.Primary600,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = NeutralColors.Neutral900
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralColors.Neutral600
                )
            }
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = NeutralColors.Neutral400
            )
        }
    }
}