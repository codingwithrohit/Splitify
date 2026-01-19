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
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<TripMember?>(null) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        SemanticColors.Error,
                                        SemanticColors.ErrorDark
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        isSelectionMode = false
                                        selectedMembers = emptySet()
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "${selectedMembers.size} selected",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        if (uiState is MembersUiState.Success) {
                                            val allMemberIds = (uiState as MembersUiState.Success)
                                                .members.map { it.id }.toSet()
                                            selectedMembers = if (selectedMembers.size == allMemberIds.size) {
                                                emptySet()
                                            } else {
                                                allMemberIds
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (uiState is MembersUiState.Success &&
                                            selectedMembers.size == (uiState as MembersUiState.Success).members.size)
                                            Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                        contentDescription = "Select All",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    enabled = selectedMembers.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = if (selectedMembers.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
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
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "Members",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (uiState is MembersUiState.Success && (uiState as MembersUiState.Success).members.isNotEmpty()) {
                                IconButton(
                                    onClick = { isSelectionMode = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Mode",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = onAddMembers,
                    containerColor = PrimaryColors.Primary600,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Members")
                }
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
                    EmptyMembersState(
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
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedMembers.contains(member.id),
                                onSelect = {
                                    selectedMembers = if (selectedMembers.contains(member.id)) {
                                        selectedMembers - member.id
                                    } else {
                                        selectedMembers + member.id
                                    }
                                },
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
                Text(
                    text = if (selectedMembers.size > 1) "Delete ${selectedMembers.size} members?" else "Delete member?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (selectedMembers.size > 1) {
                        "Are you sure you want to remove these ${selectedMembers.size} members from the trip?"
                    } else {
                        "Are you sure you want to remove ${memberToDelete?.displayName ?: "this member"} from the trip?"
                    },
                    color = NeutralColors.Neutral600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Call delete logic here
                        // For multiple: viewModel.deleteMembers(tripId, selectedMembers)
                        // For single: viewModel.deleteMember(tripId, memberToDelete?.id)

                        showDeleteDialog = false
                        isSelectionMode = false
                        selectedMembers = emptySet()
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticColors.Error
                    ),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        memberToDelete = null
                    }
                ) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberCard(
    member: TripMember,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { if (isSelectionMode) onSelect },
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.CardShape,
        color = if (isSelected) PrimaryColors.Primary50 else Color.White,
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(PrimaryColors.Primary500, PrimaryColors.Primary700)
            )
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryColors.Primary600,
                        uncheckedColor = NeutralColors.Neutral400
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) PrimaryColors.Primary100 else PrimaryColors.Primary50,
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
                    color = if (isSelected) PrimaryColors.Primary900 else MaterialTheme.colorScheme.onSurface
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

            if (!isSelectionMode) {
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
}