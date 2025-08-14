package com.example.saudeconectada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.saudeconectada.navigation.AppNavigation
import com.example.saudeconectada.ui.theme.SaudeConectadaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaudeConectadaTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}