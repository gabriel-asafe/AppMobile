package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.data.models.Doctor
import com.example.saudeconectada.data.models.Patient
import com.example.saudeconectada.data.repository.AuthRepository
import com.example.saudeconectada.data.repository.VitalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DoctorDashboardUiState {
        data class Success(
        val doctor: Doctor, 
        val vitalsByPatient: Map<String, List<Vital>> = emptyMap(),
        val patients: Map<String, Patient> = emptyMap()
    ) : DoctorDashboardUiState()
    data class Error(val message: String) : DoctorDashboardUiState()
    object Loading : DoctorDashboardUiState()
    object Idle : DoctorDashboardUiState()
}

class DoctorDashboardViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val vitalsRepository = VitalsRepository()

    private val _uiState = MutableStateFlow<DoctorDashboardUiState>(DoctorDashboardUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _linkPatientState = MutableStateFlow<AuthUiEvent>(AuthUiEvent.Idle)
    val linkPatientState = _linkPatientState.asStateFlow()

    init {
        fetchDoctorData()
    }

    fun fetchDoctorData() {
        viewModelScope.launch {
            _uiState.value = DoctorDashboardUiState.Loading
            val doctorResult = authRepository.getCurrentDoctor()

            doctorResult.onSuccess { doctor ->
                val patientDetailsMap = mutableMapOf<String, Patient>()
                doctor.linkedPatientIds.forEach { patientId ->
                    authRepository.getPatientDetails(patientId).onSuccess { patient ->
                        patientDetailsMap[patientId] = patient
                    }
                }

                val vitalsResult = vitalsRepository.getVitalsForDoctor()
                vitalsResult.onSuccess { vitals ->
                    _uiState.value = DoctorDashboardUiState.Success(doctor, vitals, patientDetailsMap)
                }.onFailure {
                    _uiState.value = DoctorDashboardUiState.Success(doctor, emptyMap(), patientDetailsMap)
                }
            }.onFailure {
                _uiState.value = DoctorDashboardUiState.Error(it.message ?: "Erro ao buscar dados do médico.")
            }
        }
    }

    fun linkPatient(patientCode: String) {
        viewModelScope.launch {
            _linkPatientState.value = AuthUiEvent.Loading
            val result = authRepository.linkPatient(patientCode)
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
