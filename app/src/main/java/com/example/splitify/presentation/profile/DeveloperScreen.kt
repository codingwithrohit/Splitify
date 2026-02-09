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
import com.example.splitify.presentation.profile.components.PDFViewerDialog
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showResumeViewer by remember { mutableStateOf(false) }
    var resumeFile by remember { mutableStateOf<File?>(null) }

    // Copy PDF from assets to cache on first composition
    LaunchedEffect(Unit) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("resume.pdf") // Your PDF in assets folder

            val file = File(context.cacheDir, "resume.pdf")
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            resumeFile = file
        } catch (e: Exception) {
            // PDF not found in assets
        }
    }

    // Replace these with your actual information
    val developerInfo = DeveloperInfo(
        name = "Your Name",
        title = "Android Developer",
        bio = "Passionate Android developer specializing in Jetpack Compose and Kotlin. I love building intuitive, user-friendly applications that solve real-world problems. Splitify is my latest project to help people manage shared expenses effortlessly.",
        email = "your.email@example.com",
        github = "https://github.com/yourusername",
        linkedin = "https://linkedin.com/in/yourusername",
        portfolio = "https://yourportfolio.com",
        skills = listOf(
            "Kotlin", "Jetpack Compose", "Android SDK", "MVVM",
            "Clean Architecture", "Coroutines", "Flow", "Room",
            "Hilt/Dagger", "Retrofit", "Git", "RESTful APIs"
        ),
        projects = listOf(
            Project(
                name = "Splitify",
                description = "Full-featured expense tracking app with real-time sync",
                tech = "Kotlin, Compose, Room, Retrofit"
            ),
            Project(
                name = "Your Other Project",
                description = "Brief description of another project you've built",
                tech = "Tech stack used"
            )
        )
    )

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = {
            SplitifyAppBar(
                title = "About Developer",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColors.Neutral50)
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Developer Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(PrimaryColors.Primary100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Developer",
                            modifier = Modifier.size(60.dp),
                            tint = PrimaryColors.Primary600
                        )
                    }

                    Text(
                        text = developerInfo.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralColors.Neutral900
                    )

                    Text(
                        text = developerInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryColors.Primary600,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = developerInfo.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralColors.Neutral700,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Resume Section
            if (resumeFile != null) {
                ProfileSection(title = "Resume") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResumeViewer = true }
                            .padding(16.dp),
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
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Resume",
                                    tint = PrimaryColors.Primary600,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "View Resume",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralColors.Neutral900
                                )
                                Text(
                                    text = "Tap to view in full screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralColors.Neutral600
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = NeutralColors.Neutral400
                        )
                    }
                }
            }

            // Social Links
            ProfileSection(title = "Connect") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    SocialLinkButton(
                        icon = Icons.Default.Language,
                        label = "Portfolio",
                        subtitle = "View my work",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(developerInfo.portfolio))
                            )
                        }
                    )

                    SocialLinkButton(
                        icon = Icons.Default.Email,
                        label = "Email",
                        subtitle = developerInfo.email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${developerInfo.email}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Skills Section
            ProfileSection(title = "Skills & Technologies") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        developerInfo.skills.forEach { skill ->
                            SkillChip(skill)
                        }
                    }
                }
            }

            // Projects Section
            ProfileSection(title = "Featured Projects") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    developerInfo.projects.forEach { project ->
                        ProjectCard(project)
                    }
                }
            }

            // Call to Action
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
        }
    }

    // PDF Resume Viewer Dialog
    if (showResumeViewer && resumeFile != null) {
        PDFViewerDialog(
            pdfFile = resumeFile!!,
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
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CustomShapes.ButtonShape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
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

                Column {
                    Text(
                        text = label,
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
        }
    }
}


@Composable
fun SkillChip(skill: String) {
    Surface(
        shape = CustomShapes.ChipShape,
        color = PrimaryColors.Primary100
    ) {
        Text(
            text = skill,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryColors.Primary700
        )
    }
}

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = NeutralColors.Neutral50
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = PrimaryColors.Primary600,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralColors.Neutral900
                )
            }

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral700
            )

            Text(
                text = "Tech: ${project.tech}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600,
                fontWeight = FontWeight.Medium
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
    val portfolio: String,
    val skills: List<String>,
    val projects: List<Project>
)

data class Project(
    val name: String,
    val description: String,
    val tech: String
)

