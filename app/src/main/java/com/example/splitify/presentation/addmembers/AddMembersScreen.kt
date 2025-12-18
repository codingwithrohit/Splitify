package com.example.splitify.presentation.addmembers

import android.graphics.drawable.Icon
import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.trips.ErrorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMembersViewModel = hiltViewModel()
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Members") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    )
    { paddingValues ->

        when(val state = uiState){
            is AddMembersUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is AddMembersUiState.Success -> {
                AddMemberContent(
                    modifier = Modifier.padding(paddingValues),
                    members = state.members,
                    onAddMember = viewModel::addMemberByName,
                    onRemoveMember = viewModel::removeMember,
                    onSearch = viewModel::searchUsers,
                    searchResults = state.searchResults,
                    isSearching = state.isSearching
                )
            }
            is AddMembersUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

    }

}

@Composable
fun AddMemberContent(
    modifier: Modifier,
    members: List<TripMember>,
    onAddMember: (String) -> Unit,
    onRemoveMember: (String, String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<TripMember>,
    isSearching: Boolean
){
    var memberName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showRemoveDialog by remember { mutableStateOf<TripMember?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier.fillMaxSize()
            .padding(16.dp)
    )
    {
        Text(
            text = "Add Member by Name",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = memberName,
                onValueChange = { memberName = it },
                label = { Text("Name") },
                placeholder = { Text("Rohit Dhanraj") },
                singleLine = true,
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

            IconButton(
                onClick = {
                    if (memberName.isNotBlank()) {
                        onAddMember(memberName)
                        memberName = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }


        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Search Existing Users",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search by username") },
            leadingIcon = { Icon(Icons.Default.Search, "Search")},
            trailingIcon = {
                if(searchQuery.isNotEmpty()){
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            onSearch("")
                        }
                    ) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            singleLine = true,

        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isSearching -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            searchQuery.isNotBlank() && searchResults.isEmpty() -> {
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            searchResults.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults, key = { it.id }) { user ->
                        MemberItem(
                            member = user,
                            onRemove = null, // ❗ reuse UI, disable remove
                            onClick = {
                                onAddMember(user.displayName)
                                searchQuery = ""
                                onSearch("")
                                keyboardController?.hide()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Current Members (${members.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if(members.isEmpty()){
            EmptyState(
                message = "No members yet",
                icon = Icons.Default.Person
            )
        }
        else{
            LazyColumn(
                Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members, key = {it.id}){ member ->
                    MemberItem(
                        member = member,
                        onRemove = { showRemoveDialog = member }
                    )
                }
            }
        }
    }
    showRemoveDialog?.let { member ->
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
            title = { Text("Remove Member?") },
            text = { Text("Remove ${member.displayName} from this trip?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveMember(member.id, member.displayName)
                        showRemoveDialog = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberItem(
    member: TripMember,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
){
    Card(
        modifier = Modifier.fillMaxWidth()
            .let {
            if (onClick != null) {
                it.clickable { onClick() }
            } else {
                it
            }
        },
        elevation = CardDefaults.cardElevation(2.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (member.isAdmin) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Admin", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Star,
                                        null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                        if (member.isGuest) {
                            Text(
                                text = "Guest",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            if (onRemove!=null && !member.isAdmin) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        CircularProgressIndicator()
    }
}