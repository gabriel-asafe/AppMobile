package com.example.saudeconectada.data.repository

import com.example.saudeconectada.data.model.Recommendation
import com.example.saudeconectada.data.model.Vital
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class VitalsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val vitalsCollection = db.collection("vitals")
    private val recommendationsCollection = db.collection("recommendations")
    private val auth = FirebaseAuth.getInstance()

    suspend fun addVital(vital: Vital): Result<Unit> {
        return try {
            vitalsCollection.add(vital).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVitalsForPatient(patientId: String): Result<List<Vital>> {
        return try {
            val query = vitalsCollection
                .whereEqualTo("patientId", patientId)
                .orderBy("date", Query.Direction.DESCENDING)
            val snapshot = query.get().await()
            val vitals = snapshot.documents.mapNotNull { document ->
                document.toObject(Vital::class.java)?.copy(id = document.id)
            }
            Result.success(vitals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVital(vitalId: String): Result<Unit> {
        return try {
            vitalsCollection.document(vitalId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVitalById(vitalId: String): Result<Vital?> {
        return try {
            val document = vitalsCollection.document(vitalId).get().await()
            val vital = document.toObject(Vital::class.java)?.copy(id = document.id)
            Result.success(vital)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVital(vital: Vital): Result<Unit> {
        return try {
            vitalsCollection.document(vital.id).set(vital).await()
            Result.success(Unit)
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

    suspend fun sendRecommendation(recommendation: Recommendation): Result<Unit> {
        return try {
            recommendationsCollection.add(recommendation).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecommendationsForPatient(patientId: String): Result<List<Recommendation>> {
        return try {
            val query = recommendationsCollection
                .whereEqualTo("patientId", patientId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
            val snapshot = query.get().await()
            val recommendations = snapshot.documents.mapNotNull { document ->
                document.toObject(Recommendation::class.java)?.copy(id = document.id)
            }
            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
