package com.example.shuttlereg.domain.usecase

import android.net.Uri
import com.example.shuttlereg.domain.model.PhotoValidationResult
import com.example.shuttlereg.domain.repository.PhotoRepository
import javax.inject.Inject

class ValidatePhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    operator fun invoke(uri: Uri): PhotoValidationResult {
        return photoRepository.validatePhoto(uri)
    }
}