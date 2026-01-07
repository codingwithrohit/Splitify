package com.example.splitify.presentation.insights

import com.example.splitify.domain.model.TripInsights

sealed interface InsightsUiState{
    data object Loading: InsightsUiState
    data class Success(
        val insights: TripInsights,
        val isGeneratingSummary: Boolean = false
    ) : InsightsUiState

    data class Error(val message: String) : InsightsUiState
}