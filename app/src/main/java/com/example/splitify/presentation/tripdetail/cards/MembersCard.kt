package com.example.splitify.presentation.tripdetail.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitify.domain.model.TripMember
import com.example.splitify.presentation.components.DashboardCard

@Composable
fun MembersCard(
    members: List<TripMember>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        title = "Members",
        icon = Icons.Default.Group,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = "${members.size} people in this trip",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Show first 5 members
        members.take(5).forEach { member ->
            Text(
                text = "• ${member.displayName}${if (member.role.name == "ADMIN") " (Admin)" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (members.size > 5) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to view all ${members.size} members →",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}