package com.example.splitify.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.splitify.presentation.theme.CustomShapes
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.SecondaryColors

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}


@Composable
fun EmptyExpensesState(
    onAddExpense: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Receipt,
        title = "No Expenses Yet",
        description = "Start adding expenses to track who paid what and split costs fairly.",
        actionLabel = if (onAddExpense != null) "Add Expense" else null,
        onAction = onAddExpense,
        modifier = modifier
    )
}

@Composable
fun EmptyMembersState(
    onAddMembers: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Group,
        title = "No Members Yet",
        description = "Add people to this trip to start splitting expenses together.",
        actionLabel = "Add Members",
        onAction = onAddMembers,
        modifier = modifier
    )
}

@Composable
fun AllSettledState(
    onViewHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = SecondaryColors.Secondary100
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = SecondaryColors.Secondary600
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All Settled! 🎉",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No pending settlements. Everyone is even!",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral600,
                textAlign = TextAlign.Center
            )

            if (onViewHistory != null) {
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.fillMaxWidth(0.65f),
                    shape = CustomShapes.ButtonShape
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View History")
                }
            }
        }
    }
}

@Composable
fun EmptySettlementsState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = NeutralColors.Neutral100
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = NeutralColors.Neutral500
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Settlements Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NeutralColors.Neutral900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Settlements will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralColors.Neutral600,
                textAlign = TextAlign.Center
            )
        }
    }
}


