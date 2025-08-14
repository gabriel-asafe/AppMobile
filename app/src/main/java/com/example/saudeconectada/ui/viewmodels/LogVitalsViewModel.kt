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
                LogVitalsUiState.Success(isSaved = true)
            } else {
                LogVitalsUiState.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }

    fun loadVital(vitalId: String) {
        viewModelScope.launch {
            _uiState.value = LogVitalsUiState.Loading
            val result = vitalsRepository.getVitalById(vitalId)
            _uiState.value = if (result.isSuccess) {
                LogVitalsUiState.Success(vital = result.getOrNull())
            } else {
                LogVitalsUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao carregar dados")
            }
        }
    }

    fun updateVital(vital: Vital) {
        viewModelScope.launch {
            _uiState.value = LogVitalsUiState.Loading
            val result = vitalsRepository.updateVital(vital)
            _uiState.value = if (result.isSuccess) {
                LogVitalsUiState.Success(isSaved = true)
            } else {
                LogVitalsUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao atualizar")
            }
        }
    }

    fun resetState() {
        _uiState.value = LogVitalsUiState.Idle
    }
}

sealed class LogVitalsUiState {
    object Idle : LogVitalsUiState()
    object Loading : LogVitalsUiState()
    data class Success(val vital: Vital? = null, val isSaved: Boolean = false) : LogVitalsUiState()
    data class Error(val message: String) : LogVitalsUiState()
}
