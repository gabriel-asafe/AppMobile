package com.example.saudeconectada.data.repository

import com.example.saudeconectada.data.model.Vital
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class VitalsRepository {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun addVital(vital: Vital): Result<Unit> {
        return try {
            val patientId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não logado."))
            val newVital = vital.copy(patientId = patientId)

            db.collection("vitals").add(newVital).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVitalsForPatient(patientId: String): Result<List<Vital>> {
        return try {
            val snapshot = db.collection("vitals")
                .whereEqualTo("patientId", patientId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val vitals = snapshot.toObjects(Vital::class.java)
            Result.success(vitals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVitalsForDoctor(): Result<Map<String, List<Vital>>> {
        return try {
            val doctorId = auth.currentUser?.uid ?: return Result.failure(Exception("Médico não logado."))
            val doctorDoc = db.collection("users").document(doctorId).get().await()
            val linkedPatientIds = doctorDoc.get("linkedPatientIds") as? List<String> ?: emptyList()

            val allVitals = mutableMapOf<String, List<Vital>>()

            for (patientId in linkedPatientIds) {
                val patientVitalsResult = getVitalsForPatient(patientId)
                if (patientVitalsResult.isSuccess) {
                    allVitals[patientId] = patientVitalsResult.getOrThrow()
                }
            }
            Result.success(allVitals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
