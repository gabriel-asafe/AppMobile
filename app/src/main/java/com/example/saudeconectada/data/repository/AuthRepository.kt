package com.example.saudeconectada.data.repository

import com.example.saudeconectada.data.models.Doctor
import com.example.saudeconectada.data.models.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

        suspend fun signUp(name: String, email: String, password: String, userType: String, crm: String? = null, specialties: List<String>? = null): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Falha ao criar usuário, UID não encontrado."))

            val userMap: HashMap<String, Any> = hashMapOf(
                "id" to uid,
                "name" to name,
                "email" to email,
                "userType" to userType
            )

            if (userType == "doctor") {
                userMap["crm"] = crm ?: ""
                userMap["specialties"] = specialties ?: emptyList<String>()
                userMap["linkedPatientIds"] = emptyList<String>()
            } else {
                // Para pacientes, o código único pode ser o próprio UID ou um código gerado
                userMap["patientCode"] = uid 
            }

            db.collection("users").document(uid).set(userMap).await()
            android.util.Log.d("AuthRepository", "Usuário criado no Firestore com UID: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Falha ao escrever dados do usuário no Firestore", e)
            // Tenta deletar o usuário criado no Auth se a escrita no Firestore falhar
            auth.currentUser?.delete()?.await()
            android.util.Log.d("AuthRepository", "Usuário deletado do Firebase Auth após falha na escrita do Firestore.")
            Result.failure(e)
        }
    }

    suspend fun getCurrentDoctor(): Result<Doctor> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não logado."))
            val document = db.collection("users").document(uid).get().await()
            if (document != null && document.exists()) {
                val doctor = Doctor(
                    uid = document.id,
                    name = document.getString("name")!!,
                    email = document.getString("email")!!,
                    crm = document.getString("crm")!!,
                    linkedPatientIds = document.get("linkedPatientIds") as? List<String> ?: emptyList()
                )
                Result.success(doctor)
            } else {
                Result.failure(Exception("Médico não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkPatient(patientCode: String): Result<Unit> {
        return try {
            val doctorUid = auth.currentUser?.uid ?: throw Exception("Médico não está logado.")

            // 1. Encontrar o paciente pelo código na coleção 'users'
            val patientQuery = db.collection("users")
                .whereEqualTo("patientCode", patientCode)
                .whereEqualTo("userType", "patient") // Garante que estamos buscando um paciente
                .limit(1)
                .get()
                .await()

            if (patientQuery.isEmpty) {
                throw Exception("Nenhum paciente encontrado com este código.")
            }

            val patientUid = patientQuery.documents.first().id

            // 2. Adicionar o UID do paciente à lista do médico na coleção 'users'
            db.collection("users").document(doctorUid)
                .update("linkedPatientIds", com.google.firebase.firestore.FieldValue.arrayUnion(patientUid))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentPatient(): Result<Patient> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não logado."))
            val document = db.collection("users").document(uid).get().await()
            if (document != null && document.exists()) {
                val patient = Patient(
                    uid = document.id,
                    name = document.getString("name")!!,
                    email = document.getString("email")!!,
                    patientCode = document.getString("patientCode")!!
                )
                Result.success(patient)
            } else {
                Result.failure(Exception("Paciente não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPatientDetails(patientId: String): Result<Patient> {
        return try {
            val document = db.collection("users").document(patientId).get().await()
            if (document != null && document.exists()) {
                val patient = Patient(
                    uid = document.id,
                    name = document.getString("name")!!,
                    email = document.getString("email")!!,
                    patientCode = document.getString("patientCode")!!
                )
                Result.success(patient)
            } else {
                Result.failure(Exception("Paciente não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Usuário não encontrado."))

            val document = db.collection("users").document(user.uid).get().await()

            if (document != null && document.exists()) {
                val userType = document.getString("userType") ?: return Result.failure(Exception("Tipo de usuário não definido."))
                Result.success(userType)
            } else {
                Result.failure(Exception("Dados do usuário não encontrados no banco de dados."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
