package com.example.shuttlereg.domain.model

import android.net.Uri

data class PhotoUploadResult(
    val downloadUrl: String,
    val uploadProgress: Float = 1f
)

sealed class PhotoValidationResult {
    object Valid : PhotoValidationResult()
    data class Error(val message: String) : PhotoValidationResult()
}

data class PhotoUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val downloadUrl: String? = null
)

data class PhotoSelection(
    val uri: Uri,
    val isValid: Boolean = false,
    val validationError: String? = null
)

enum class PhotoSource {
    CAMERA, GALLERY
}

object PhotoConstants {
    const val MAX_FILE_SIZE_MB = 10
    const val MIN_RESOLUTION = 200
    const val COMPRESSION_QUALITY = 80
    const val MAX_DIMENSION = 800
    
    val ALLOWED_MIME_TYPES = listOf("image/jpeg", "image/png")
}