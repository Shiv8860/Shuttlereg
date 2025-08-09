package com.example.shuttlereg.data.repository

import android.net.Uri
import com.example.shuttlereg.data.photo.PhotoManager
import com.example.shuttlereg.domain.model.PhotoValidationResult
import com.example.shuttlereg.domain.repository.PhotoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoManager: PhotoManager
) : PhotoRepository {
    
    override suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return photoManager.uploadProfilePhoto(userId, photoUri, onProgress)
    }
    
    override suspend fun uploadTournamentPhoto(
        tournamentId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return photoManager.uploadTournamentPhoto(tournamentId, photoUri, onProgress)
    }
    
    override suspend fun deleteProfilePhoto(userId: String): Result<Unit> {
        return photoManager.deleteProfilePhoto(userId)
    }
    
    override suspend fun deleteTournamentPhoto(tournamentId: String): Result<Unit> {
        return photoManager.deleteTournamentPhoto(tournamentId)
    }
    
    override fun validatePhoto(uri: Uri): PhotoValidationResult {
        return photoManager.validatePhoto(uri)
    }
    
    override suspend fun downloadImageBytes(url: String): Result<ByteArray> {
        return photoManager.downloadImageBytes(url)
    }
}