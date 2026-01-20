package com.example.splitify.presentation.addmembers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.components.*
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    tripId: String,
    onBack: () -> Unit,
    onAddMembers: () -> Unit,
    viewModel: MembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<TripMember?>(null) }

    Scaffold(
        topBar = {
            SplitifyAppBar(
                title = "Members",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMembers,
                containerColor = PrimaryColors.Primary600,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Members")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is MembersUiState.Loading -> {
                LoadingScreen(
                    message = "Loading members...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            is MembersUiState.Success -> {
                if (state.members.isEmpty()) {
                    EmptyMemberScreen(
                        onAddMembers = onAddMembers,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.members,
                            key = { it.id }
                        ) { member ->
                            MemberCard(
                                member = member,
                                onDelete = {
                                    memberToDelete = member
                                    showDeleteDialog = true
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            is MembersUiState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = { },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = SemanticColors.Error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = "Delete member?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${memberToDelete?.displayName ?: "this member"} from the trip?",
                    color = NeutralColors.Neutral600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // viewModel.deleteMember(tripId, memberToDelete?.id)
                        showDeleteDialog = false
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticColors.Error),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    memberToDelete = null
                }) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }
}

@Composable
private fun MemberCard(
    member: TripMember,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.CardShape,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = PrimaryColors.Primary50,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryColors.Primary600
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (member.userId != null) "App user" else "Guest",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralColors.Neutral600
                )
            }

            if (member.role == MemberRole.ADMIN) {
                Surface(
                    shape = CustomShapes.ChipShape,
                    color = PrimaryColors.Primary100
                ) {
                    Text(
                        text = "Admin",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColors.Primary700
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = SemanticColors.Error
                )
            }
        }
    }
}