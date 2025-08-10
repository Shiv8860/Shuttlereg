package com.example.shuttlereg.data.repository

import com.example.shuttlereg.domain.model.User
import com.example.shuttlereg.domain.model.Gender
import com.example.shuttlereg.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid)
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithPhone(phoneNumber: String): Result<String> {
        return try {
            // This would typically involve PhoneAuthProvider.OnVerificationStateChangedCallbacks
            // For now, returning a placeholder verification ID
            // In a real implementation, you'd set up the phone auth flow
            Result.success("verification_id_placeholder")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid)
                Result.success(user)
            } else {
                Result.failure(Exception("Phone verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Check if user exists in Firestore, if not create new user
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                
                val user = if (userDoc.exists()) {
                    getUserFromFirestore(firebaseUser.uid)
                } else {
                    // Create new user from Google account info
                    val newUser = User(
                        id = firebaseUser.uid,
                        fullName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        phone = firebaseUser.phoneNumber ?: "",
                        isEmailVerified = firebaseUser.isEmailVerified
                    )
                    createUserInFirestore(newUser)
                    newUser
                }
                
                Result.success(user)
            } else {
                Result.failure(Exception("Google sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Create user in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    fullName = fullName,
                    email = email,
                    isEmailVerified = firebaseUser.isEmailVerified
                )
                
                createUserInFirestore(user)
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun isUserSignedIn(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Delete user data from Firestore
                firestore.collection("users").document(user.uid).delete().await()
                // Delete Firebase Auth account
                user.delete().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null && user.email != null) {
                // Re-authenticate user first
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()
                
                // Update password
                user.updatePassword(newPassword).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getUserFromFirestore(userId: String): User {
        val userDoc = firestore.collection("users").document(userId).get().await()
        return if (userDoc.exists()) {
            val data = userDoc.data ?: emptyMap()
            User(
                id = userId,
                fullName = data["fullName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                gender = try {
                    Gender.valueOf((data["gender"] as? String ?: "MALE").uppercase())
                } catch (e: Exception) {
                    Gender.MALE
                },
                clubName = data["clubName"] as? String,
                profilePhotoUrl = data["profilePhotoUrl"] as? String,
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                isEmailVerified = data["isEmailVerified"] as? Boolean ?: false,
                isPhoneVerified = data["isPhoneVerified"] as? Boolean ?: false
            )
        } else {
            // Return default user if not found in Firestore
            User(id = userId)
        }
    }

    private suspend fun createUserInFirestore(user: User) {
        val userData = mapOf(
            "fullName" to user.fullName,
            "email" to user.email,
            "phone" to user.phone,
            "gender" to user.gender.name,
            "clubName" to user.clubName,
            "profilePhotoUrl" to user.profilePhotoUrl,
            "createdAt" to user.createdAt,
            "isEmailVerified" to user.isEmailVerified,
            "isPhoneVerified" to user.isPhoneVerified
        )
        
        firestore.collection("users").document(user.id).set(userData).await()
    }
}