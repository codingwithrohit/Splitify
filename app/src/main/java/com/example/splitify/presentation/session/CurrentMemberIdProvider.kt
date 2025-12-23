package com.example.splitify.presentation.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.tripdetail.TripDetailViewModel

@Composable
fun CurrentMemberIdProvider(
    tripId: String,
    content: @Composable (String) -> Unit
) {
    val viewModel: TripDetailViewModel = hiltViewModel()
    val currentMemberId by viewModel.currentMemberId.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        viewModel.loadCurrentMemberId()
    }
    when {
        currentMemberId != null -> content(currentMemberId!!)
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}