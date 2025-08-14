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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saudeconectada.ui.viewmodels.PatientDashboardUiState
import com.example.saudeconectada.ui.viewmodels.PatientDashboardViewModel

@Composable
fun PatientDashboardScreen(
    navController: NavController,
    viewModel: PatientDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = uiState) {
            is PatientDashboardUiState.Loading -> {
                CircularProgressIndicator()
            }
            is PatientDashboardUiState.Success -> {
                val patient = state.patient
                val clipboardManager = LocalClipboardManager.current

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
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy, 
                                    contentDescription = "Copiar Código"
                                )
                            }
                        }
                    }
                }
            }
            is PatientDashboardUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is PatientDashboardUiState.Idle -> {
                // Pode mostrar um estado inicial ou não fazer nada
            }
        }
    }
}
