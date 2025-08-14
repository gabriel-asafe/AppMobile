package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.model.Recommendation
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.data.models.Patient
import com.example.saudeconectada.data.repository.AuthRepository
import com.example.saudeconectada.data.repository.VitalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PatientDashboardUiState {
        data class Success(val patient: Patient, val vitals: List<Vital>, val recommendations: List<Recommendation>) : PatientDashboardUiState()
    data class Error(val message: String) : PatientDashboardUiState()
    object Loading : PatientDashboardUiState()
    object Idle : PatientDashboardUiState()
}

class PatientDashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val vitalsRepository: VitalsRepository = VitalsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientDashboardUiState>(PatientDashboardUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        loadPatientData()
    }

    fun loadPatientData() {
        _uiState.value = PatientDashboardUiState.Loading
        viewModelScope.launch {
            val patientResult = authRepository.getCurrentPatient()
            patientResult.fold(
                onSuccess = { patient ->
                    val vitalsResult = vitalsRepository.getVitalsForPatient(patient.uid)
                    vitalsResult.fold(
                        onSuccess = { vitals ->
                            val recommendationsResult = vitalsRepository.getRecommendationsForPatient(patient.uid)
                            recommendationsResult.fold(
                                onSuccess = { recommendations ->
                                    _uiState.value = PatientDashboardUiState.Success(patient, vitals, recommendations)
                                },
                                onFailure = { exception ->
                                    _uiState.value = PatientDashboardUiState.Error(exception.message ?: "Erro ao buscar recomendações")
                                }
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = PatientDashboardUiState.Error(exception.message ?: "Erro ao buscar sinais vitais")
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.value = PatientDashboardUiState.Error(exception.message ?: "Erro ao buscar dados do paciente")
                }
            )
        }
    }

    fun deleteVital(vitalId: String) {
        viewModelScope.launch {
            val result = vitalsRepository.deleteVital(vitalId)
            if (result.isSuccess) {
                loadPatientData() // Recarrega os dados após a exclusão
            } else {
                // TODO: Tratar erro de exclusão, talvez com um novo estado de UI para mostrar um Toast/Snackbar
            }
        }
    }
}
