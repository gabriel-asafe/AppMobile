package com.example.saudeconectada.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.data.model.Recommendation
import com.example.saudeconectada.data.model.Vital
import com.example.saudeconectada.data.models.Patient
import com.example.saudeconectada.navigation.Screen
import com.example.saudeconectada.ui.theme.ThemeManager
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme
import com.example.saudeconectada.ui.viewmodels.PatientDashboardUiState
import com.example.saudeconectada.ui.viewmodels.PatientDashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    navController: NavController,
    viewModel: PatientDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vitalsUpdated = navController.currentBackStackEntry
        ?.savedStateHandle?.getLiveData<Boolean>("vitals_updated")?.observeAsState()

    LaunchedEffect(vitalsUpdated?.value) {
        if (vitalsUpdated?.value == true) {
            viewModel.loadPatientData()
            navController.currentBackStackEntry?.savedStateHandle?.set("vitals_updated", false)
        }
    }

    SaudeConectadaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Painel do Paciente") },
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
                    is PatientDashboardUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is PatientDashboardUiState.Success -> {
                        PatientDashboard(
                            patient = state.patient,
                            vitals = state.vitals,
                            recommendations = state.recommendations,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                    is PatientDashboardUiState.Error -> {
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                clipboardManager.setText(AnnotatedString(state.message))
                                Toast.makeText(context, "Erro copiado!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Copiar Erro")
                            }
                        }
                    }
                    is PatientDashboardUiState.Idle -> {}
                }
            }
        }
    }
}

@Composable
fun PatientDashboard(
    patient: Patient,
    vitals: List<Vital>,
    recommendations: List<Recommendation>,
    navController: NavController,
    viewModel: PatientDashboardViewModel
) {
    var vitalToDelete by remember { mutableStateOf<Vital?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (vitalToDelete != null) {
        AlertDialog(
            onDismissRequest = { vitalToDelete = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza de que deseja excluir este registro?") },
            confirmButton = {
                Button(onClick = {
                    vitalToDelete?.id?.let { viewModel.deleteVital(it) }
                    vitalToDelete = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                Button(onClick = { vitalToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Bem-vindo(a), ${patient.name}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(patient.email, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Compartilhe este código com seu médico:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = patient.patientCode,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(patient.patientCode))
                            Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copiar Código",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(Screen.LogVitals.createRoute(patient.uid)) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Registrar Sinais Vitais", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Meus Últimos Registros", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (vitals.isEmpty()) {
            item {
                Text("Nenhum sinal vital registrado ainda.", modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(vitals) { vital ->
                VitalCard(
                    vital = vital,
                    onEditClick = { navController.navigate(Screen.LogVitals.createEditRoute(patient.uid, vital.id)) },
                    onDeleteClick = { vitalToDelete = vital }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Recomendações Médicas", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (recommendations.isEmpty()) {
            item {
                Text("Nenhuma recomendação recebida ainda.", modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(recommendations) { recommendation ->
                RecommendationCard(recommendation = recommendation)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val date = recommendation.timestamp.toDate()
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(
                text = "Recebido em: ${format.format(date)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun VitalCard(
    vital: Vital,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val date = vital.date.toDate()
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(
                text = "Registrado em: ${format.format(date)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Frequência Cardíaca: ${vital.heartRate} bpm")
            Text("Pressão Arterial: ${vital.bloodPressure} mmHg")
            Text("Temperatura: ${vital.temperature} °C")
            Text("Glicose: ${vital.glucose} mg/dL")
            Text("Peso: ${vital.weight} kg")
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
