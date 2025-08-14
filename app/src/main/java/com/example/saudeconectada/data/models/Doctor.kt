package com.example.saudeconectada.data.models

data class Doctor(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val crm: String = "",
    val userType: String = "doctor",
    val linkedPatientIds: List<String> = emptyList()
)
