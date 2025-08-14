package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.model.Vital
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PatientVitalsUiState {
    object Loading : PatientVitalsUiState()
    data class Success(val vitals: List<Vital>) : PatientVitalsUiState()
    data class Error(val message: String) : PatientVitalsUiState()
}

class PatientVitalsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PatientVitalsUiState>(PatientVitalsUiState.Loading)
    val uiState: StateFlow<PatientVitalsUiState> = _uiState

    private val db = FirebaseFirestore.getInstance()

    fun fetchVitals(patientId: String) {
        viewModelScope.launch {
            _uiState.value = PatientVitalsUiState.Loading
            try {
                db.collection("vitals")
                    .whereEqualTo("patientId", patientId)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val vitalsList = documents.toObjects(Vital::class.java)
                        _uiState.value = PatientVitalsUiState.Success(vitalsList)
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = PatientVitalsUiState.Error("Erro ao buscar dados: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = PatientVitalsUiState.Error("Erro: ${e.message}")
            }
        }
    }
}
