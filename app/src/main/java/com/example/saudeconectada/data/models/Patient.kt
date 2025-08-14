package com.example.saudeconectada.data.models

data class Patient(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val patientCode: String = "", // Código para o médico vincular
    val userType: String = "patient"
)
