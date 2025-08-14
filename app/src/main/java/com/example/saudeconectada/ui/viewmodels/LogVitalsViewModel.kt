package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.data.repository.VitalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogVitalsViewModel(private val vitalsRepository: VitalsRepository = VitalsRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<LogVitalsUiState>(LogVitalsUiState.Idle)
    val uiState: StateFlow<LogVitalsUiState> = _uiState

    fun addVital(vital: Vital) {
        viewModelScope.launch {
            _uiState.value = LogVitalsUiState.Loading
            val result = vitalsRepository.addVital(vital)
            _uiState.value = if (result.isSuccess) {
                LogVitalsUiState.Success
            } else {
                LogVitalsUiState.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed class LogVitalsUiState {
    object Idle : LogVitalsUiState()
    object Loading : LogVitalsUiState()
    object Success : LogVitalsUiState()
    data class Error(val message: String) : LogVitalsUiState()
}
