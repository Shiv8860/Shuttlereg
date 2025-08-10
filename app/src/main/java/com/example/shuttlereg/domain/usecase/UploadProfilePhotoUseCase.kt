package com.example.shuttlereg.domain.usecase

import android.net.Uri
import com.example.shuttlereg.domain.repository.PhotoRepository
import javax.inject.Inject

class UploadProfilePhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(
        userId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        // Validate photo first
        val validationResult = photoRepository.validatePhoto(photoUri)
        if (validationResult is com.example.shuttlereg.domain.model.PhotoValidationResult.Error) {
            return Result.failure(Exception(validationResult.message))
        }
        
        // Upload photo
        return photoRepository.uploadProfilePhoto(userId, photoUri, onProgress)
    }
}