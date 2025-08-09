package com.example.shuttlereg.data.repository

import com.example.shuttlereg.domain.model.Registration
import com.example.shuttlereg.domain.model.PaymentData
import com.example.shuttlereg.domain.model.PaymentStatus
import com.example.shuttlereg.domain.model.RegistrationStatus
import com.example.shuttlereg.domain.repository.RegistrationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistrationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RegistrationRepository {

    private val registrationsCollection = firestore.collection("registrations")

    override suspend fun createRegistration(registration: Registration): Result<String> {
        return try {
            val registrationWithTimestamp = registration.copy(
                registrationDate = LocalDateTime.now(),
                status = RegistrationStatus.SUBMITTED,
                updatedAt = System.currentTimeMillis()
            )
            
            val docRef = registrationsCollection.add(registrationWithTimestamp).await()
            val registrationWithId = registrationWithTimestamp.copy(id = docRef.id)
            docRef.set(registrationWithId).await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRegistration(registration: Registration): Result<Unit> {
        return try {
            val updatedRegistration = registration.copy(
                updatedAt = System.currentTimeMillis()
            )
            registrationsCollection.document(registration.id)
                .set(updatedRegistration)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRegistrationById(registrationId: String): Result<Registration> {
        return try {
            val document = registrationsCollection.document(registrationId).get().await()
            if (document.exists()) {
                val registration = document.toObject(Registration::class.java)
                    ?: throw Exception("Failed to parse registration")
                Result.success(registration)
            } else {
                Result.failure(Exception("Registration not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserRegistrations(userId: String): Flow<List<Registration>> = callbackFlow {
        val listener = registrationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val registrations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Registration::class.java)
                } ?: emptyList()
                
                trySend(registrations)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getTournamentRegistrations(tournamentId: String): Flow<List<Registration>> = callbackFlow {
        val listener = registrationsCollection
            .whereEqualTo("tournamentId", tournamentId)
            .whereEqualTo("status", RegistrationStatus.CONFIRMED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val registrations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Registration::class.java)
                } ?: emptyList()
                
                trySend(registrations)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun processPayment(registrationId: String, paymentData: PaymentData): Result<Unit> {
        return try {
            val updates = mapOf(
                "paymentId" to paymentData.paymentId,
                "paymentStatus" to PaymentStatus.SUCCESS.name,
                "status" to RegistrationStatus.CONFIRMED.name,
                "updatedAt" to System.currentTimeMillis()
            )
            
            registrationsCollection.document(registrationId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateRegistrationPDF(registrationId: String): Result<String> {
        return try {
            // TODO: Implement PDF generation logic
            // This would involve:
            // 1. Fetch registration details
            // 2. Generate PDF using iText7
            // 3. Upload to Firebase Storage
            // 4. Update registration with PDF URL
            
            val pdfUrl = "https://example.com/registration_$registrationId.pdf"
            
            val updates = mapOf(
                "pdfUrl" to pdfUrl,
                "updatedAt" to System.currentTimeMillis()
            )
            
            registrationsCollection.document(registrationId)
                .update(updates)
                .await()
            
            Result.success(pdfUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRegistration(registrationId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to RegistrationStatus.CANCELLED.name,
                "paymentStatus" to PaymentStatus.REFUNDED.name,
                "updatedAt" to System.currentTimeMillis()
            )
            
            registrationsCollection.document(registrationId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDraftRegistration(registration: Registration): Result<Unit> {
        return try {
            val draftRegistration = registration.copy(
                status = RegistrationStatus.DRAFT,
                updatedAt = System.currentTimeMillis()
            )
            
            if (registration.id.isEmpty()) {
                // Create new draft
                val docRef = registrationsCollection.add(draftRegistration).await()
                val registrationWithId = draftRegistration.copy(id = docRef.id)
                docRef.set(registrationWithId).await()
            } else {
                // Update existing draft
                registrationsCollection.document(registration.id)
                    .set(draftRegistration)
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDraftRegistration(userId: String, tournamentId: String): Result<Registration?> {
        return try {
            val query = registrationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("tournamentId", tournamentId)
                .whereEqualTo("status", RegistrationStatus.DRAFT.name)
                .limit(1)
                .get()
                .await()
            
            val registration = query.documents.firstOrNull()?.toObject(Registration::class.java)
            Result.success(registration)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}