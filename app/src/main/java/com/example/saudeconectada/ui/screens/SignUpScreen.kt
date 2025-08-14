package com.example.saudeconectada.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.navigation.Screen
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme
import com.example.saudeconectada.ui.viewmodels.AuthUiEvent
import com.example.saudeconectada.ui.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(
    navController: NavController,
    userType: String,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var crm by remember { mutableStateOf("") }
    var specialtiesText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authEvent by authViewModel.authUiEvent.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authEvent) {
        when (val event = authEvent) {
            is AuthUiEvent.Success -> {
                isLoading = false
                Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Login.route) {
                    // Limpa a pilha de navegação até a tela de seleção de perfil
                    popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                }
                authViewModel.resetAuthEvent()
            }
            is AuthUiEvent.Error -> {
                isLoading = false
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthEvent()
            }
            is AuthUiEvent.Loading -> {
                isLoading = true
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro de ${if (userType == "doctor") "Médico" else "Paciente"}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (userType == "doctor") {
            OutlinedTextField(
                value = crm,
                onValueChange = { crm = it },
                label = { Text("CRM") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = specialtiesText,
                onValueChange = { specialtiesText = it },
                label = { Text("Especialidades (separadas por vírgula)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                val isDoctor = userType == "doctor"
                if (name.isBlank() || email.isBlank() || password.isBlank() || (isDoctor && crm.isBlank())) {
                    Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                } else {
                    val specialties = if (isDoctor) specialtiesText.split(",").map { it.trim() } else null
                    authViewModel.signUp(name, email, password, userType, if (isDoctor) crm else null, specialties)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Cadastrar")
            }
        }
    }
}


