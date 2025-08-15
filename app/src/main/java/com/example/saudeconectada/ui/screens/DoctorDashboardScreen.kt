package com.example.saudeconectada.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import com.example.saudeconectada.ui.theme.ThemeManager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.navigation.Screen
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme
import com.example.saudeconectada.ui.viewmodels.AuthUiEvent
import com.example.saudeconectada.ui.viewmodels.DoctorDashboardUiState
import com.example.saudeconectada.ui.viewmodels.DoctorDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    navController: NavController,
    viewModel: DoctorDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val linkState by viewModel.linkPatientState.collectAsState()
    val context = LocalContext.current

    var patientCode by remember { mutableStateOf("") }
    var isLinking by remember { mutableStateOf(false) }

    LaunchedEffect(linkState) {
        when (val event = linkState) {
            is AuthUiEvent.Loading -> isLinking = true
            is AuthUiEvent.Success -> {
                isLinking = false
                patientCode = ""
                Toast.makeText(context, "Paciente vinculado com sucesso!", Toast.LENGTH_SHORT).show()
                viewModel.resetLinkPatientEvent()
            }
            is AuthUiEvent.Error -> {
                isLinking = false
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                viewModel.resetLinkPatientEvent()
            }
            else -> isLinking = false
        }
    }

    SaudeConectadaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Painel do Médico") },
                    actions = {
                        IconButton(onClick = { ThemeManager.toggleTheme() }) {
                            Icon(
                                imageVector = if (ThemeManager.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Mudar tema"
                            )
                        }
                        IconButton(onClick = { /* TODO: Logout */ }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Sair")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(it),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val state = uiState) {
                    is DoctorDashboardUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is DoctorDashboardUiState.Success -> {
                        val doctor = state.doctor
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                Text("Dr(a). ${doctor.name}", style = MaterialTheme.typography.headlineMedium)
                                Text(doctor.email, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(24.dp))

                                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Vincular novo paciente", style = MaterialTheme.typography.titleLarge)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = patientCode,
                                            onValueChange = { patientCode = it },
                                            label = { Text("Código do Paciente") },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !isLinking
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { if (patientCode.isNotBlank()) viewModel.linkPatient(patientCode) },
                                            modifier = Modifier.align(Alignment.End),
                                            enabled = !isLinking
                                        ) {
                                            if (isLinking) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                            } else {
                                                Text("Vincular")
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Meus Pacientes", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (doctor.linkedPatientIds.isEmpty()) {
                                item {
                                    Text("Nenhum paciente vinculado ainda.")
                                }
                            } else {
                                items(doctor.linkedPatientIds) { patientId ->
                                    val patientName = state.patients[patientId]?.name ?: "Paciente (ID: $patientId)"
                                    val patientVitals = state.vitalsByPatient[patientId] ?: emptyList()
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable { navController.navigate(Screen.PatientVitals.createRoute(patientId)) },
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(patientName, style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            if (patientVitals.isEmpty()) {
                                                Text("Nenhum dado vital registrado.")
                                            } else {
                                                val latestVital = patientVitals.first()
                                                Text("Último Registro:", style = MaterialTheme.typography.labelLarge)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("  • Frequência Cardíaca: ${latestVital.heartRate} bpm")
                                                Text("  • Pressão Arterial: ${latestVital.bloodPressure} mmHg")
                                                Text("  • Temperatura: ${latestVital.temperature} °C")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is DoctorDashboardUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is DoctorDashboardUiState.Idle -> { }
                }
            }
        }
    }
}
