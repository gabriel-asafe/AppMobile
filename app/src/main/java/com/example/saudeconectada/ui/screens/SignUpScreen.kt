package com.example.saudeconectada.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
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
                    popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                }
                authViewModel.resetAuthEvent()
            }
            is AuthUiEvent.Error -> {
                isLoading = false
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthEvent()
            }
            is AuthUiEvent.Loading -> isLoading = true
            else -> Unit
        }
    }

    SaudeConectadaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cadastro de ${if (userType == "doctor") "Médico" else "Paciente"}") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(it),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome Completo") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                        Spacer(modifier = Modifier.height(16.dp))
                        if (userType == "doctor") {
                            OutlinedTextField(
                                value = crm,
                                onValueChange = { crm = it },
                                label = { Text("CRM") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
                                    Toast.makeText(context, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                                } else {
                                    val specialties = if (isDoctor) specialtiesText.split(",").map { it.trim() } else null
                                    authViewModel.signUp(name, email, password, userType, if (isDoctor) crm else null, specialties)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Cadastrar", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


