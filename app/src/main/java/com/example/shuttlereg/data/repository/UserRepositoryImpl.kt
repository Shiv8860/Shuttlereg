package com.example.shuttlereg.data.repository

import com.example.shuttlereg.domain.model.PartnerInfo
import com.example.shuttlereg.domain.model.User
import com.example.shuttlereg.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    override suspend fun getCurrentUser(): Flow<User?> = callbackFlow {
        var userDocListener: com.google.firebase.firestore.ListenerRegistration? = null
        
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Listen to user document changes
                userDocListener = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        
                        if (snapshot != null && snapshot.exists()) {
                            val user = mapDocumentToUser(snapshot.data ?: emptyMap(), firebaseUser.uid)
                            trySend(user)
                        } else {
                            trySend(null)
                        }
                    }
            } else {
                trySend(null)
            }
        }
        
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { 
            firebaseAuth.removeAuthStateListener(listener)
            userDocListener?.remove()
        }
    }

    override suspend fun getCurrentUserSync(): User? {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return null
            val document = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (document.exists()) {
                mapDocumentToUser(document.data ?: emptyMap(), firebaseUser.uid)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = mapDocumentToUser(document.data ?: emptyMap(), userId)
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userData = mapUserToDocument(user)
            firestore.collection("users").document(user.id).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(user: User): Result<String> {
        return try {
            val userData = mapUserToDocument(user)
            val docRef = if (user.id.isNotEmpty()) {
                firestore.collection("users").document(user.id)
            } else {
                firestore.collection("users").document()
            }
            
            docRef.set(userData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Delete user document
            firestore.collection("users").document(userId).delete().await()
            
            // Delete profile photo if exists
            try {
                storage.reference.child("profile_photos/$userId").delete().await()
            } catch (e: Exception) {
                // Ignore if photo doesn't exist
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePhoto(userId: String, photoBytes: ByteArray): Result<String> {
        return try {
            val photoRef = storage.reference.child("profile_photos/$userId")
            val uploadTask = photoRef.putBytes(photoBytes).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            // Update user document with photo URL
            firestore.collection("users").document(userId)
                .update("profilePhotoUrl", downloadUrl.toString()).await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfilePhoto(userId: String): Result<Unit> {
        return try {
            // Delete from storage
            storage.reference.child("profile_photos/$userId").delete().await()
            
            // Update user document
            firestore.collection("users").document(userId)
                .update("profilePhotoUrl", null).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePartnerInfo(userId: String, partnerInfo: PartnerInfo): Result<Unit> {
        return try {
            val partnerData = mapOf(
                "id" to partnerInfo.id,
                "name" to partnerInfo.name,
                "phone" to partnerInfo.phone,
                "email" to partnerInfo.email,
                "gender" to partnerInfo.gender.name,
                "isRegistered" to partnerInfo.isRegistered,
                "savedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users").document(userId)
                .collection("partners").document(partnerInfo.id)
                .set(partnerData).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPartnerHistory(userId: String): Flow<List<PartnerInfo>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("partners")
            .orderBy("savedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val partners = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        PartnerInfo(
                            id = data["id"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            phone = data["phone"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            gender = try {
                                com.example.shuttlereg.domain.model.Gender.valueOf(
                                    (data["gender"] as? String ?: "MALE").uppercase()
                                )
                            } catch (e: Exception) {
                                com.example.shuttlereg.domain.model.Gender.MALE
                            },
                            isRegistered = data["isRegistered"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(partners)
            }
        
        awaitClose { listener.remove() }
    }

    private fun mapDocumentToUser(data: Map<String, Any>, userId: String): User {
        return User(
            id = userId,
            fullName = data["fullName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            phone = data["phone"] as? String ?: "",
            dateOfBirth = (data["dateOfBirth"] as? String)?.let {
                try {
                    java.time.LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            gender = try {
                com.example.shuttlereg.domain.model.Gender.valueOf(
                    (data["gender"] as? String ?: "MALE").uppercase()
                )
            } catch (e: Exception) {
                com.example.shuttlereg.domain.model.Gender.MALE
            },
            clubName = data["clubName"] as? String,
            profilePhotoUrl = data["profilePhotoUrl"] as? String,
            eligibleEvents = (data["eligibleEvents"] as? List<String>)?.mapNotNull { eventName ->
                try {
                    com.example.shuttlereg.domain.model.EventCategory.valueOf(eventName)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList(),
            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
            isEmailVerified = data["isEmailVerified"] as? Boolean ?: false,
            isPhoneVerified = data["isPhoneVerified"] as? Boolean ?: false
        )
    }

    private fun mapUserToDocument(user: User): Map<String, Any?> {
        return mapOf(
            "fullName" to user.fullName,
            "email" to user.email,
            "phone" to user.phone,
            "dateOfBirth" to user.dateOfBirth?.toString(),
            "gender" to user.gender.name,
            "clubName" to user.clubName,
            "profilePhotoUrl" to user.profilePhotoUrl,
            "eligibleEvents" to user.eligibleEvents.map { it.name },
            "createdAt" to user.createdAt,
            "isEmailVerified" to user.isEmailVerified,
            "isPhoneVerified" to user.isPhoneVerified,
            "updatedAt" to System.currentTimeMillis()
        )
    }
}