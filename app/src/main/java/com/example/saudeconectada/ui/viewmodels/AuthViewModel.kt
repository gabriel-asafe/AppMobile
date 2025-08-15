package com.example.saudeconectada.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saudeconectada.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class AuthUiEvent {
    data class Success(val userType: String) : AuthUiEvent()
    data class Error(val message: String) : AuthUiEvent()
    object Loading : AuthUiEvent()
    object Idle : AuthUiEvent()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authUiEvent = MutableStateFlow<AuthUiEvent>(AuthUiEvent.Idle)
    val authUiEvent = _authUiEvent.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            android.util.Log.d("AuthViewModel", "Tentando login com Email: $email")
            _authUiEvent.value = AuthUiEvent.Loading
            val result = repository.login(email, password)
            result.onSuccess {
                android.util.Log.d("AuthViewModel", "Login bem-sucedido para o usuário: $email, Tipo: $it")
                _authUiEvent.value = AuthUiEvent.Success(it)
            }.onFailure {
                android.util.Log.e("AuthViewModel", "Falha no login para o usuário: $email", it)
                _authUiEvent.value = AuthUiEvent.Error(it.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }

    fun signUp(name: String, email: String, password: String, userType: String, crm: String? = null, specialties: List<String>? = null) {
        viewModelScope.launch {
            _authUiEvent.value = AuthUiEvent.Loading
            val result = repository.signUp(name, email, password, userType, crm, specialties)
            result.onSuccess {

                _authUiEvent.value = AuthUiEvent.Success("login") // Sinaliza para ir para a tela de login
            }.onFailure {
                _authUiEvent.value = AuthUiEvent.Error(it.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }
    
    fun resetAuthEvent() {
        _authUiEvent.value = AuthUiEvent.Idle
    }
}
