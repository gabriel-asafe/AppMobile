package com.example.saudeconectada.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import com.example.saudeconectada.navigation.Screen
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.ui.viewmodels.AuthUiEvent
import com.example.saudeconectada.ui.viewmodels.DoctorDashboardUiState
import com.example.saudeconectada.ui.viewmodels.DoctorDashboardViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val state = uiState) {
            is DoctorDashboardUiState.Loading -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            }
            is DoctorDashboardUiState.Success -> {
                val doctor = state.doctor
                Text("Dr(a). ${doctor.name}", style = MaterialTheme.typography.headlineSmall)
                Text(doctor.email, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))

                // Seção para adicionar paciente
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
                            onClick = { 
                                if(patientCode.isNotBlank()) viewModel.linkPatient(patientCode) 
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = !isLinking
                        ) {
                            if (isLinking) {
                                CircularProgressIndicator(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Vincular")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Lista de pacientes vinculados
                Text("Meus Pacientes", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                val patients = state.patients
                val vitalsByPatient = state.vitalsByPatient

                if (doctor.linkedPatientIds.isEmpty()) {
                    Text("Nenhum paciente vinculado ainda.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(doctor.linkedPatientIds) { patientId ->
                            val patientName = patients[patientId]?.name ?: "Paciente (ID: $patientId)"
                            val patientVitals = vitalsByPatient[patientId] ?: emptyList()
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.PatientVitals.createRoute(patientId)) },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(patientName, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (patientVitals.isEmpty()) {
                                        Text("Nenhum dado vital registrado.")
                                    } else {
                                        // Displaying only the latest vital for brevity
                                        val latestVital = patientVitals.first()
                                        Text("Último Registro:", style = MaterialTheme.typography.labelLarge)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("  • Frequência Cardíaca: ${latestVital.heartRate} bpm")
                                        Text("  • Pressão Arterial: ${latestVital.bloodPressure} mmHg")
                                        Text("  • Temperatura: ${latestVital.temperature} °C")
                                        Text("  • Glicose: ${latestVital.glucose} mg/dL")
                                        Text("  • Peso: ${latestVital.weight} kg")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is DoctorDashboardUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is DoctorDashboardUiState.Idle -> { }
        }
    }
}
