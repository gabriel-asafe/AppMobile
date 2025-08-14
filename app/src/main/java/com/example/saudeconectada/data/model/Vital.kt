package com.example.saudeconectada.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Vital(
    val id: String = "",
    val patientId: String = "",
    val date: Timestamp = Timestamp(Date()),
    val heartRate: Int = 0,
    val bloodPressure: String = "",
    val temperature: Double = 0.0,
    val glucose: Int = 0,
    val weight: Double = 0.0
)
