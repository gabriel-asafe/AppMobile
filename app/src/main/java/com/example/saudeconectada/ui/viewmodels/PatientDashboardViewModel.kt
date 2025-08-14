package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.models.Patient
import com.example.saudeconectada.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PatientDashboardUiState {
    data class Success(val patient: Patient) : PatientDashboardUiState()
    data class Error(val message: String) : PatientDashboardUiState()
    object Loading : PatientDashboardUiState()
    object Idle : PatientDashboardUiState()
}

class PatientDashboardViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<PatientDashboardUiState>(PatientDashboardUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        fetchPatientData()
    }

    private fun fetchPatientData() {
        viewModelScope.launch {
            _uiState.value = PatientDashboardUiState.Loading
            val result = repository.getCurrentPatient()
            result.onSuccess { patient ->
                _uiState.value = PatientDashboardUiState.Success(patient)
            }.onFailure {
                _uiState.value = PatientDashboardUiState.Error(it.message ?: "Erro ao buscar dados do paciente.")
            }
        }
    }
}
