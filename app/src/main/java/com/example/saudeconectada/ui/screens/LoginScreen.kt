package com.example.saudeconectada.ui.screens

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
import android.widget.Toast

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authEvent by authViewModel.authUiEvent.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authEvent) {
        when (val event = authEvent) {
            is AuthUiEvent.Success -> {
                isLoading = false
                val route = if (event.userType == "doctor") {
                    Screen.DoctorDashboard.route
                } else {
                    Screen.PatientDashboard.route
                }
                navController.navigate(route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                authViewModel.resetAuthEvent() // Reseta o evento para evitar re-navegação
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
            text = "Saúde Conectada",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email, password)
                } else {
                    Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Entrar")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate(Screen.ProfileSelection.route) },
            enabled = !isLoading
        ) {
            Text("Não tem uma conta? Cadastre-se")
        }
    }
}
