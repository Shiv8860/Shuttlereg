package com.example.shuttlereg.domain.repository

import com.example.shuttlereg.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): Flow<User?>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun createUser(user: User): Result<String>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun uploadProfilePhoto(userId: String, photoBytes: ByteArray): Result<String>
    suspend fun deleteProfilePhoto(userId: String): Result<Unit>
    suspend fun savePartnerInfo(userId: String, partnerInfo: com.example.shuttlereg.domain.model.PartnerInfo): Result<Unit>
    suspend fun getPartnerHistory(userId: String): Flow<List<com.example.shuttlereg.domain.model.PartnerInfo>>
}