package com.example.saudeconectada.data.model

import com.google.firebase.Timestamp

data class Recommendation(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
