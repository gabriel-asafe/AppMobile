package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.data.repository.VitalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PatientVitalsUiState {
    object Loading : PatientVitalsUiState()
    data class Success(val vitals: List<Vital>) : PatientVitalsUiState()
    data class Error(val message: String) : PatientVitalsUiState()
}

class PatientVitalsViewModel(
    private val vitalsRepository: VitalsRepository = VitalsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientVitalsUiState>(PatientVitalsUiState.Loading)
    val uiState: StateFlow<PatientVitalsUiState> = _uiState



    public fun loadVitals(patientId: String) {
        viewModelScope.launch {
            _uiState.value = PatientVitalsUiState.Loading
            val result = vitalsRepository.getVitalsForPatient(patientId)
            result.fold(
                onSuccess = { vitals: List<Vital> -> _uiState.value = PatientVitalsUiState.Success(vitals) },
                onFailure = { exception: Throwable -> _uiState.value = PatientVitalsUiState.Error(exception.message ?: "Erro ao carregar sinais vitais") }
            )
        }
    }
}
