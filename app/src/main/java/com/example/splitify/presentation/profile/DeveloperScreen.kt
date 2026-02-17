package com.example.splitify.presentation.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.splitify.R
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.profile.components.FullScreenPdfViewer
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    var showResumeViewer by remember { mutableStateOf(false) }
    var resumeFile by remember { mutableStateOf<File?>(null) }

    // Copy resume from assets
    LaunchedEffect(Unit) {
        try {
            val inputStream = context.assets.open("resume.pdf")
            val file = File(context.cacheDir, "resume.pdf")

            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            resumeFile = file
        } catch (_: Exception) { }
    }

    val developerInfo = DeveloperInfo(
        name = "Rohit Kumar",
        title = "Android Developer",
        bio = "Passionate Android developer specializing in Jetpack Compose and Kotlin. I love building intuitive, user-friendly applications that solve real-world problems.",
        email = "dev.rohitkumar21@gmail.com",
        github = "https://github.com/codingwithrohit",
        linkedin = "https://www.linkedin.com/in/rohit-kumar-11138620a/",
        portfolio = "https://yourportfolio.com"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            SplitifyAppBar(
                title = "About Developer",
                onBackClick = onBack
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColors.Neutral50)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(PrimaryColors.Primary100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = PrimaryColors.Primary600
                        )
                    }

                    Text(
                        developerInfo.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralColors.Neutral900
                    )

                    Text(
                        developerInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColors.Primary600
                    )

                    Text(
                        developerInfo.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral700,
                        textAlign = TextAlign.Center
                    )
                }
            }


            if (resumeFile != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResumeViewer = true },
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryColors.Primary100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = PrimaryColors.Primary600
                                )
                            }

                            Column {
                                Text(
                                    "View Resume",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Tap to open full screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralColors.Neutral600
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NeutralColors.Neutral400
                        )
                    }
                }
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "Connect With Me",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SocialLinkButton(
                        icon = painterResource(R.drawable.github),
                        label = "GitHub",
                        subtitle = "View my code",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(developerInfo.github))
                            )
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    SocialLinkButton(
                        icon = painterResource(R.drawable.linkedin_50),
                        label = "LinkedIn",
                        subtitle = "Professional profile",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(developerInfo.linkedin))
                            )
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    SocialLinkButton(
                        icon = Icons.Default.Email,
                        label = "Email",
                        subtitle = developerInfo.email,
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:${developerInfo.email}")
                                )
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryColors.Primary50
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = PrimaryColors.Primary600,
                        modifier = Modifier.size(32.dp)
                    )

                    Text(
                        text = "Interested in collaborating?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColors.Primary700
                    )

                    Text(
                        text = "I'm always open to new opportunities and interesting projects!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral700,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${developerInfo.email}")
                                putExtra(Intent.EXTRA_SUBJECT, "Let's collaborate!")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColors.Primary600
                        ),
                        shape = CustomShapes.ButtonShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Get in Touch",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

        }
    }


    if (showResumeViewer && resumeFile != null) {
        FullScreenPdfViewer(
            file = resumeFile!!,
            onDismiss = { showResumeViewer = false }
        )
    }
}

@Composable
fun SocialLinkButton(
    icon: Any,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = NeutralColors.Neutral50
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    when (icon) {
                        is ImageVector -> Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = PrimaryColors.Primary600
                        )
                        is Painter -> Icon(
                            painter = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = PrimaryColors.Primary600
                        )
                    }
                }

                // Text
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralColors.Neutral900
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralColors.Neutral600
                    )
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = NeutralColors.Neutral400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class DeveloperInfo(
    val name: String,
    val title: String,
    val bio: String,
    val email: String,
    val github: String,
    val linkedin: String,
    val portfolio: String
)