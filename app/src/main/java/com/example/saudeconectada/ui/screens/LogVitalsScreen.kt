package com.example.saudeconectada.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.ui.viewmodels.LogVitalsUiState
import com.example.saudeconectada.ui.viewmodels.LogVitalsViewModel

@Composable
fun LogVitalsScreen(
    navController: NavController,
    patientId: String,
    viewModel: LogVitalsViewModel = viewModel()
) {
    var heartRate by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var glucose by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LogVitalsUiState.Success) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrar Sinais Vitais", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = heartRate, onValueChange = { heartRate = it }, label = { Text("Frequência Cardíaca (bpm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = bloodPressure, onValueChange = { bloodPressure = it }, label = { Text("Pressão Arterial (mmHg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = temperature, onValueChange = { temperature = it }, label = { Text("Temperatura (°C)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = glucose, onValueChange = { glucose = it }, label = { Text("Glicose (mg/dL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Peso (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val vital = Vital(
                    patientId = patientId,
                    heartRate = heartRate.toIntOrNull() ?: 0,
                    bloodPressure = bloodPressure,
                    temperature = temperature.toDoubleOrNull() ?: 0.0,
                    glucose = glucose.toIntOrNull() ?: 0,
                    weight = weight.toDoubleOrNull() ?: 0.0
                )
                viewModel.addVital(vital)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LogVitalsUiState.Loading
        ) {
            if (uiState is LogVitalsUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Salvar")
            }
        }

        if (uiState is LogVitalsUiState.Error) {
            Text((uiState as LogVitalsUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
