package com.example.shuttlereg.domain.repository

import com.example.shuttlereg.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithPhone(phoneNumber: String): Result<String> // Returns verification ID
    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getCurrentUserId(): String?
    suspend fun isUserSignedIn(): Flow<Boolean>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadUser(): Result<Unit>
}