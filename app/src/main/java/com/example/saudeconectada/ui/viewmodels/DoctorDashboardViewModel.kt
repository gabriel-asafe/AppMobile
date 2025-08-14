package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.models.Doctor
import com.example.saudeconectada.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DoctorDashboardUiState {
    data class Success(val doctor: Doctor) : DoctorDashboardUiState()
    data class Error(val message: String) : DoctorDashboardUiState()
    object Loading : DoctorDashboardUiState()
    object Idle : DoctorDashboardUiState()
}

class DoctorDashboardViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<DoctorDashboardUiState>(DoctorDashboardUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _linkPatientState = MutableStateFlow<AuthUiEvent>(AuthUiEvent.Idle)
    val linkPatientState = _linkPatientState.asStateFlow()

    init {
        fetchDoctorData()
    }

    fun fetchDoctorData() {
        viewModelScope.launch {
            _uiState.value = DoctorDashboardUiState.Loading
            val result = repository.getCurrentDoctor()
            result.onSuccess { doctor ->
                _uiState.value = DoctorDashboardUiState.Success(doctor)
            }.onFailure {
                _uiState.value = DoctorDashboardUiState.Error(it.message ?: "Erro ao buscar dados do médico.")
            }
        }
    }

    fun linkPatient(patientCode: String) {
        viewModelScope.launch {
            _linkPatientState.value = AuthUiEvent.Loading
            val result = repository.linkPatient(patientCode)
            result.onSuccess {
                _linkPatientState.value = AuthUiEvent.Success("PatientLinked")
                fetchDoctorData() // Atualiza os dados do médico para mostrar o novo paciente
            }.onFailure {
                _linkPatientState.value = AuthUiEvent.Error(it.message ?: "Erro ao vincular paciente.")
            }
        }
    }

    fun resetLinkPatientEvent() {
        _linkPatientState.value = AuthUiEvent.Idle
    }
}
