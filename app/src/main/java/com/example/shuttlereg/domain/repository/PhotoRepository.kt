package com.example.shuttlereg.domain.repository

import android.net.Uri
import com.example.shuttlereg.domain.model.PhotoValidationResult

interface PhotoRepository {
    suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String>
    
    suspend fun uploadTournamentPhoto(
        tournamentId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String>
    
    suspend fun deleteProfilePhoto(userId: String): Result<Unit>
    
    suspend fun deleteTournamentPhoto(tournamentId: String): Result<Unit>
    
    fun validatePhoto(uri: Uri): PhotoValidationResult
    
    suspend fun downloadImageBytes(url: String): Result<ByteArray>
}