package com.example.saudeconectada.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.ui.viewmodels.PatientVitalsUiState
import com.example.saudeconectada.ui.viewmodels.PatientVitalsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatientVitalsScreen(
    navController: NavController,
    patientId: String,
    viewModel: PatientVitalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.fetchVitals(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Histórico de Sinais Vitais", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is PatientVitalsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PatientVitalsUiState.Success -> {
                val vitals = state.vitals
                if (vitals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum dado vital registrado para este paciente.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(vitals) { vital ->
                            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(vital.date.toDate())
                                    Text(formattedDate, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Frequência Cardíaca: ${vital.heartRate} bpm")
                                    Text("Pressão Arterial: ${vital.bloodPressure} mmHg")
                                    Text("Temperatura: ${vital.temperature} °C")
                                    Text("Glicose: ${vital.glucose} mg/dL")
                                    Text("Peso: ${vital.weight} kg")
                                }
                            }
                        }
                    }
                }
            }
            is PatientVitalsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
