package com.example.splitify.presentation.addmembers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.MemberRole
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.components.*

/**
 * Full-screen Members list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    tripId: String,
    onBack: () -> Unit,
    onAddMembers: () -> Unit,
    viewModel: MembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Members") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMembers) {
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
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.members,
                            key = { it.id }
                        ) { member ->
                            MemberCard(member = member)
                        }

                        // Bottom padding for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            is MembersUiState.Error -> {
                ErrorStateWithRetry(
                    message = state.message,
                    onRetry = { /* Auto-retry */ },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: TripMember,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (member.userId != null) "App user" else "Guest",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (member.role == MemberRole.ADMIN) {
                AssistChip(
                    onClick = { },
                    label = { Text("Admin") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}



