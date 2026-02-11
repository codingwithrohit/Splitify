package com.example.splitify.presentation.addmembers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.TripMember
import com.example.splitify.domain.model.User
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors
import com.example.splitify.presentation.theme.SecondaryColors
import com.example.splitify.presentation.theme.SemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMembersViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SplitifyAppBar(
                title = "Add Members",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        when(val state = uiState){
            is AddMembersUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is AddMembersUiState.Success -> {
                val isCurrentUserAdmin = state.members.find { it.userId == currentUserId }?.isAdmin == true
                AddMemberContent(
                    modifier = Modifier.padding(paddingValues),
                    members = state.members,
                    isCurrentUserAdmin = isCurrentUserAdmin,
                    onAddMember = viewModel::addMemberByName,
                    onRemoveMember = viewModel::removeMember,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onClearSearch = viewModel::clearSearch,
                    searchResults = state.searchResults,
                    isSearching = state.isSearching,
                    searchQuery = state.searchQuery,
                    hasSearched = state.hasSearched
                )
            }
            is AddMembersUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SemanticColors.Error
                        )
                        Text(
                            text = state.message,
                            color = SemanticColors.Error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun AddMemberContent(
    modifier: Modifier,
    members: List<TripMember>,
    isCurrentUserAdmin: Boolean,
    searchQuery: String,
    searchResults: List<User>,
    isSearching: Boolean,
    hasSearched: Boolean,
    onAddMember: (String) -> Unit,
    onRemoveMember: (String, String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var showRemoveDialog by remember { mutableStateOf<TripMember?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        SectionHeader(
            icon = Icons.Default.Person,
            title = "Add Member by Name"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = memberName,
            onValueChange = { memberName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., John Doe", color = NeutralColors.Neutral400) },
            singleLine = true,
            shape = CustomShapes.TextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColors.Primary500,
                unfocusedBorderColor = NeutralColors.Neutral300,
                focusedContainerColor = PrimaryColors.Primary50,
                unfocusedContainerColor = Color.White
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (memberName.isNotBlank()) {
                            onAddMember(memberName)
                            memberName = ""
                            keyboardController?.hide()
                        }
                    },
                    enabled = memberName.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add",
                        tint = if (memberName.isNotBlank())
                            PrimaryColors.Primary600
                        else
                            NeutralColors.Neutral400
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (memberName.isNotBlank()) {
                        onAddMember(memberName)
                        memberName = ""
                        keyboardController?.hide()
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

//        SectionHeader(
//            icon = Icons.Default.Search,
//            title = "Search Existing Users"
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = onSearchQueryChange,
//            modifier = Modifier.fillMaxWidth(),
//            placeholder = { Text("Type to search...", color = NeutralColors.Neutral400) },
//            leadingIcon = {
//                Icon(Icons.Default.Search, "Search", tint = PrimaryColors.Primary500)
//            },
//            trailingIcon = {
//                if (searchQuery.isNotEmpty()) {
//                    IconButton(onClick = onClearSearch) {
//                        Icon(Icons.Default.Clear, "Clear", tint = NeutralColors.Neutral600)
//                    }
//                }
//            },
//            singleLine = true,
//            shape = CustomShapes.TextFieldShape,
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = PrimaryColors.Primary500,
//                unfocusedBorderColor = NeutralColors.Neutral300,
//                focusedContainerColor = PrimaryColors.Primary50,
//                unfocusedContainerColor = Color.White
//            )
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        when {
//            isSearching -> {
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = CustomShapes.CardShape,
//                    color = NeutralColors.Neutral100
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(20.dp),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(20.dp),
//                            color = PrimaryColors.Primary600,
//                            strokeWidth = 2.dp
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            "Searching...",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = NeutralColors.Neutral700
//                        )
//                    }
//                }
//            }
//
//            hasSearched && searchResults.isEmpty() && searchQuery.isNotBlank() -> {
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = CustomShapes.CardShape,
//                    color = SemanticColors.ErrorLight
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(20.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Search,
//                            contentDescription = null,
//                            tint = SemanticColors.ErrorDark,
//                            modifier = Modifier.size(32.dp)
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            "No users found for \"$searchQuery\"",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = SemanticColors.ErrorDark,
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }
//            }
//
//            searchResults.isNotEmpty() -> {
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = CustomShapes.CardShape,
//                    color = SecondaryColors.Secondary50,
//                    shadowElevation = 2.dp
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(12.dp)
//                    ) {
//                        Text(
//                            "Found ${searchResults.size} user(s)",
//                            style = MaterialTheme.typography.labelLarge,
//                            fontWeight = FontWeight.SemiBold,
//                            color = SecondaryColors.Secondary700,
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
//                        )
//
//                        LazyColumn(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .heightIn(max = 220.dp),
//                            contentPadding = PaddingValues(4.dp),
//                            verticalArrangement = Arrangement.spacedBy(6.dp)
//                        ) {
//                            items(searchResults, key = { it.id }) { user ->
//                                SearchResultUserItem(
//                                    user = user,
//                                    onClick = {
//                                        onAddMember(user.userName)
//                                        onClearSearch()
//                                        keyboardController?.hide()
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Members",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                shape = CircleShape,
                color = PrimaryColors.Primary100
            ) {
                Text(
                    text = "${members.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColors.Primary700
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (members.isEmpty()) {
            EmptyState(
                message = "No members yet",
                icon = Icons.Default.Person
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(members, key = { it.id }) { member ->
                    MemberItem(
                        member = member,
                        onRemove = if(isCurrentUserAdmin && !member.isAdmin){
                            { showRemoveDialog = member }
                        }
                        else null
                    )
                }
            }
        }
    }

    showRemoveDialog?.let { member ->
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
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
                    "Remove Member?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Remove ${member.displayName} from this trip?",
                    color = NeutralColors.Neutral600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveMember(member.id, member.displayName)
                        showRemoveDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SemanticColors.Error
                    ),
                    shape = CustomShapes.ButtonShape
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = null }) {
                    Text("Cancel", color = NeutralColors.Neutral600)
                }
            },
            shape = CustomShapes.DialogShape
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.medium,
            color = PrimaryColors.Primary100
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = PrimaryColors.Primary600
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun MemberItem(
    member: TripMember,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
){
    Surface(
        modifier = Modifier.fillMaxWidth()
            .let {
                if (onClick != null) {
                    it.clickable { onClick() }
                } else {
                    it
                }
            },
        shape = CustomShapes.CardShape,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryColors.Primary100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColors.Primary700
                    )
                }

                Column {
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (member.isAdmin) {
                            Surface(
                                shape = CustomShapes.ChipShape,
                                color = PrimaryColors.Primary100
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        null,
                                        modifier = Modifier.size(12.dp),
                                        tint = PrimaryColors.Primary700
                                    )
                                    Text(
                                        "Admin",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryColors.Primary700
                                    )
                                }
                            }
                        }
                        else{
                            Text(
                                text = if (member.userId != null) "App user" else "Guest",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralColors.Neutral600
                            )
                        }
                    }
                }
            }

            if (onRemove != null && !member.isAdmin) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = SemanticColors.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultUserItem(
    user: User,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CustomShapes.CardShape,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SecondaryColors.Secondary100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.userName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryColors.Secondary700
                    )
                }

                Column {
                    Text(
                        text = user.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (user.fullName != null) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralColors.Neutral600
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add user",
                tint = SecondaryColors.Secondary600,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = NeutralColors.Neutral100
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(20.dp),
                tint = NeutralColors.Neutral500
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralColors.Neutral600
        )
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = PrimaryColors.Primary600,
            strokeWidth = 3.dp
        )
    }
}