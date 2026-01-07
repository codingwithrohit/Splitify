package com.example.splitify.presentation.insights

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.usecase.insights.GetTripInsightsUseCase
import com.example.splitify.presentation.insights.InsightsUiState.*
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getTripInsightsUseCase: GetTripInsightsUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"]){
        "Trip ID is required for InsightsViewModel"
    }

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights(){
        viewModelScope.launch {
            _uiState.value = InsightsUiState.Loading

            when(val result = getTripInsightsUseCase(tripId)){
                is Result.Success -> {
                    _uiState.value = Success(
                        insights = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = Error(
                        message = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun refresh(){
        loadInsights()
    }

    fun generateSummary(onComplete: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState !is InsightsUiState.Success) return

        Log.d("InsightsVM", "📝 Generating summary...")
        _uiState.value = currentState.copy(isGeneratingSummary = true)

        viewModelScope.launch {
            // TODO: Implement summary generation in Commit 7
            // For now, just placeholder
            _uiState.value = currentState.copy(isGeneratingSummary = false)
        }
    }
}