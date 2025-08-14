package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.model.Recommendation
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.data.repository.AuthRepository
import com.example.saudeconectada.data.repository.VitalsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PatientVitalsUiState {
    object Loading : PatientVitalsUiState()
    data class Success(val vitals: List<Vital>) : PatientVitalsUiState()
    data class Error(val message: String) : PatientVitalsUiState()
}

sealed class RecommendationUiEvent {
    object Idle : RecommendationUiEvent()
    object Loading : RecommendationUiEvent()
    data class Success(val message: String) : RecommendationUiEvent()
    data class Error(val message: String) : RecommendationUiEvent()
}

class PatientVitalsViewModel(
    private val vitalsRepository: VitalsRepository = VitalsRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<PatientVitalsUiState>(PatientVitalsUiState.Loading)
    val uiState: StateFlow<PatientVitalsUiState> = _uiState

    private val _recommendationState = MutableStateFlow<RecommendationUiEvent>(RecommendationUiEvent.Idle)
    val recommendationState: StateFlow<RecommendationUiEvent> = _recommendationState



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

    fun sendRecommendation(patientId: String, text: String) {
        viewModelScope.launch {
            _recommendationState.value = RecommendationUiEvent.Loading
            val doctorId = auth.currentUser?.uid ?: run {
                _recommendationState.value = RecommendationUiEvent.Error("Médico não autenticado.")
                return@launch
            }

            val recommendation = Recommendation(
                patientId = patientId,
                doctorId = doctorId,
                text = text
            )

            val result = vitalsRepository.sendRecommendation(recommendation)
            result.fold(
                onSuccess = {
                    _recommendationState.value = RecommendationUiEvent.Success("Recomendação enviada com sucesso!")
                },
                onFailure = { exception ->
                    _recommendationState.value = RecommendationUiEvent.Error(exception.message ?: "Erro ao enviar recomendação.")
                }
            )
        }
    }

    fun resetRecommendationState() {
        _recommendationState.value = RecommendationUiEvent.Idle
    }
}
