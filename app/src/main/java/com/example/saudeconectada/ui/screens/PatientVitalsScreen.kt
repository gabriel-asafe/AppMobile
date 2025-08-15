package com.example.saudeconectada.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import com.example.saudeconectada.ui.theme.ThemeManager
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme
import com.example.saudeconectada.ui.viewmodels.PatientVitalsUiState
import com.example.saudeconectada.ui.viewmodels.RecommendationUiEvent
import com.example.saudeconectada.ui.viewmodels.PatientVitalsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientVitalsScreen(
    navController: NavController,
    patientId: String,
    viewModel: PatientVitalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()
    val context = LocalContext.current

    var recommendationText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.loadVitals(patientId)
    }

    LaunchedEffect(recommendationState) {
        when (val state = recommendationState) {
            is RecommendationUiEvent.Loading -> isSending = true
            is RecommendationUiEvent.Success -> {
                isSending = false
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                recommendationText = ""
                viewModel.resetRecommendationState()
            }
            is RecommendationUiEvent.Error -> {
                isSending = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetRecommendationState()
            }
            else -> isSending = false
        }
    }

    SaudeConectadaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Histórico de Sinais Vitais") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        IconButton(onClick = { ThemeManager.toggleTheme() }) {
                            Icon(
                                imageVector = if (ThemeManager.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Mudar tema"
                            )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {

                when (val state = uiState) {
                    is PatientVitalsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is PatientVitalsUiState.Success -> {
                        val vitals = state.vitals
                        LazyColumn(
                            modifier = Modifier.weight(1.0f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (vitals.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Nenhum dado vital registrado.")
                                    }
                                }
                            } else {
                                items(vitals) { vital ->
                                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            val date = vital.date.toDate()
                                            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                            Text(format.format(date), style = MaterialTheme.typography.titleMedium)
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
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = recommendationText,
                            onValueChange = { recommendationText = it },
                            label = { Text("Adicionar Recomendação") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                if (recommendationText.isNotBlank()) {
                                    viewModel.sendRecommendation(patientId, recommendationText) 
                                } else {
                                    Toast.makeText(context, "A recomendação não pode estar vazia.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isSending,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Enviar Recomendação")
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
    }
}
