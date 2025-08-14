package com.example.saudeconectada.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme
import com.example.saudeconectada.ui.viewmodels.LogVitalsUiState
import com.example.saudeconectada.ui.viewmodels.LogVitalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogVitalsScreen(
    navController: NavController,
    patientId: String,
    vitalId: String?,
    viewModel: LogVitalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var heartRate by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var glucose by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    LaunchedEffect(vitalId) {
        if (vitalId != null) {
            viewModel.loadVital(vitalId)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is LogVitalsUiState.Success) {
            if (state.vital != null) {
                state.vital.let {
                    heartRate = it.heartRate.toString()
                    bloodPressure = it.bloodPressure
                    temperature = it.temperature.toString()
                    glucose = it.glucose.toString()
                    weight = it.weight.toString()
                }
            }
            if (state.isSaved) {
                navController.previousBackStackEntry?.savedStateHandle?.set("vitals_updated", true)
                navController.popBackStack()
                viewModel.resetState()
            }
        }
    }

    SaudeConectadaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (vitalId == null) "Registrar Sinais Vitais" else "Editar Sinais Vitais") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
                modifier = Modifier.fillMaxSize().padding(it)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
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
                                    id = vitalId ?: "",
                                    patientId = patientId,
                                    heartRate = heartRate.toIntOrNull() ?: 0,
                                    bloodPressure = bloodPressure,
                                    temperature = temperature.toDoubleOrNull() ?: 0.0,
                                    glucose = glucose.toIntOrNull() ?: 0,
                                    weight = weight.toDoubleOrNull() ?: 0.0
                                )
                                if (vitalId == null) {
                                    viewModel.addVital(vital)
                                } else {
                                    viewModel.updateVital(vital)
                                }
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
            }
        }
    }
}
