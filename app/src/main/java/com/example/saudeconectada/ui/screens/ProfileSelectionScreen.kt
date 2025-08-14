package com.example.saudeconectada.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saudeconectada.navigation.Screen
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(navController: NavController) {
    SaudeConectadaTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Selecione o Perfil") },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Como você irá usar o app?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = { navController.navigate(Screen.SignUp.createRoute("doctor")) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Sou Médico", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { navController.navigate(Screen.SignUp.createRoute("patient")) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Sou Paciente", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
