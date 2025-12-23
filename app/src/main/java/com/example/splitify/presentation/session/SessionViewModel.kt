package com.example.splitify.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val supabase: SupabaseClient
): ViewModel() {

    private val _currentMemberId = MutableStateFlow<String?>(null)
    val currentMemberId: StateFlow<String?> = _currentMemberId

    init {
        observeSession()
    }

    private fun observeSession(){
        viewModelScope.launch {
            val session = supabase.auth.currentSessionOrNull()
            _currentMemberId.value = session?.user?.id
        }
    }

    fun clearSession(){
        _currentMemberId.value = null
    }
}