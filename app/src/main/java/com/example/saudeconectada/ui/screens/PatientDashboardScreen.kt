package com.example.saudeconectada.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.ui.viewmodels.PatientDashboardUiState
import com.example.saudeconectada.navigation.Screen
import com.example.saudeconectada.data.models.Patient
import com.example.saudeconectada.ui.viewmodels.PatientDashboardViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.saudeconectada.data.model.Vital
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatientDashboardScreen(
    navController: NavController,
    viewModel: PatientDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentBackStackEntry = navController.currentBackStackEntry
    val vitalsUpdated = currentBackStackEntry
        ?.savedStateHandle?.getLiveData<Boolean>("vitals_updated")?.observeAsState()

    LaunchedEffect(vitalsUpdated?.value) {
        if (vitalsUpdated?.value == true) {
            viewModel.loadPatientData()
            navController.currentBackStackEntry?.savedStateHandle?.set("vitals_updated", false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is PatientDashboardUiState.Loading -> {
                CircularProgressIndicator()
            }
            is PatientDashboardUiState.Success -> {
                PatientDashboard(
                    patient = state.patient,
                    vitals = state.vitals,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            is PatientDashboardUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is PatientDashboardUiState.Idle -> {
                // Pode mostrar um estado inicial ou nada
            }
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
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(vital.date.toDate())
            Text(formattedDate, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Frequência Cardíaca: ${vital.heartRate} bpm")
            Text("Pressão Arterial: ${vital.bloodPressure} mmHg")
            Text("Temperatura: ${vital.temperature} °C")
            Text("Glicose: ${vital.glucose} mg/dL")
            Text("Peso: ${vital.weight} kg")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PatientDashboard(
    patient: Patient,
    vitals: List<Vital>,
    navController: NavController,
    viewModel: PatientDashboardViewModel
) {
    var vitalToDelete by remember { mutableStateOf<Vital?>(null) }

    if (vitalToDelete != null) {
        AlertDialog(
            onDismissRequest = { vitalToDelete = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza de que deseja excluir este registro de sinais vitais?") },
            confirmButton = {
                Button(
                    onClick = {
                        vitalToDelete?.id?.let { viewModel.deleteVital(it) }
                        vitalToDelete = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                Button(onClick = { vitalToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bem-vindo(a), ${patient.name}", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(patient.email, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Compartilhe este código com seu médico:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = patient.patientCode,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Sinais Vitais")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Meus Últimos Registros", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (vitals.isEmpty()) {
            Text("Nenhum sinal vital registrado ainda.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vitals) { vital ->
                    VitalCard(
                        vital = vital,
                        onEditClick = {
                            navController.navigate(Screen.LogVitals.createEditRoute(patient.uid, vital.id))
                        },
                        onDeleteClick = { vitalToDelete = vital }
                    )
                }
            }
        }
    }
}
