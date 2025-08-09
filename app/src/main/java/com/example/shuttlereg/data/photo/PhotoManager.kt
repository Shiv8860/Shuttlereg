package com.example.shuttlereg.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.shuttlereg.domain.model.PhotoConstants
import com.example.shuttlereg.domain.model.PhotoUploadResult
import com.example.shuttlereg.domain.model.PhotoValidationResult
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class PhotoManager @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    
    suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate photo first
            val validationResult = validatePhoto(photoUri)
            if (validationResult is PhotoValidationResult.Error) {
                return@withContext Result.failure(Exception(validationResult.message))
            }
            
            // Compress and resize image
            val compressedImage = compressImage(photoUri)
            
            // Create storage reference
            val photoRef = storage.reference
                .child("profile_photos")
                .child("$userId.jpg")
            
            // Upload with progress tracking
            val uploadTask = photoRef.putBytes(compressedImage)
            
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                onProgress(progress / 100f)
            }
            
            val snapshot = uploadTask.await()
            val downloadUrl = snapshot.storage.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadTournamentPhoto(
        tournamentId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val validationResult = validatePhoto(photoUri)
            if (validationResult is PhotoValidationResult.Error) {
                return@withContext Result.failure(Exception(validationResult.message))
            }
            
            val compressedImage = compressImage(photoUri)
            
            val photoRef = storage.reference
                .child("tournament_photos")
                .child("$tournamentId.jpg")
            
            val uploadTask = photoRef.putBytes(compressedImage)
            
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                onProgress(progress / 100f)
            }
            
            val snapshot = uploadTask.await()
            val downloadUrl = snapshot.storage.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun compressImage(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        
        // Resize to max dimensions while maintaining aspect ratio
        val resizedBitmap = resizeBitmap(
            originalBitmap, 
            PhotoConstants.MAX_DIMENSION, 
            PhotoConstants.MAX_DIMENSION
        )
        
        // Compress to JPEG with specified quality
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG, 
            PhotoConstants.COMPRESSION_QUALITY, 
            outputStream
        )
        
        // Clean up
        originalBitmap.recycle()
        resizedBitmap.recycle()
        
        outputStream.toByteArray()
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape orientation
            val newW = min(maxWidth, width)
            val newH = (newW / aspectRatio).toInt()
            newW to newH
        } else {
            // Portrait or square orientation
            val newH = min(maxHeight, height)
            val newW = (newH * aspectRatio).toInt()
            newW to newH
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    suspend fun deleteProfilePhoto(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val photoRef = storage.reference
                .child("profile_photos")
                .child("$userId.jpg")
            
            photoRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTournamentPhoto(tournamentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val photoRef = storage.reference
                .child("tournament_photos")
                .child("$tournamentId.jpg")
            
            photoRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun validatePhoto(uri: Uri): PhotoValidationResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            val fileSize = getFileSize(uri)
            val maxFileSizeBytes = PhotoConstants.MAX_FILE_SIZE_MB * 1024 * 1024
            
            when {
                fileSize > maxFileSizeBytes ->
                    PhotoValidationResult.Error("File size too large (max ${PhotoConstants.MAX_FILE_SIZE_MB}MB)")
                
                options.outWidth < PhotoConstants.MIN_RESOLUTION || options.outHeight < PhotoConstants.MIN_RESOLUTION ->
                    PhotoValidationResult.Error("Image resolution too low (min ${PhotoConstants.MIN_RESOLUTION}x${PhotoConstants.MIN_RESOLUTION})")
                
                options.outMimeType !in PhotoConstants.ALLOWED_MIME_TYPES ->
                    PhotoValidationResult.Error("Invalid file format (JPEG/PNG only)")
                
                options.outWidth == -1 || options.outHeight == -1 ->
                    PhotoValidationResult.Error("Invalid image file")
                
                else -> PhotoValidationResult.Valid
            }
        } catch (e: Exception) {
            PhotoValidationResult.Error("Unable to process image: ${e.message}")
        }
    }
    
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                var size = 0L
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    size += bytesRead
                }
                size
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    suspend fun downloadImageBytes(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.getReferenceFromUrl(url)
            val maxDownloadSize = 10L * 1024 * 1024 // 10MB
            val bytes = storageRef.getBytes(maxDownloadSize).await()
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}