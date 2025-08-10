# Photo Management Implementation Guide

## 📸 Photo Capture & Management System

### 1. Photo Capture UI Component

```kotlin
// presentation/ui/components/PhotoCaptureComponent.kt
@Composable
fun PhotoCaptureComponent(
    currentPhotoUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo captured successfully
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onPhotoSelected(it) }
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoUri = createImageUri(context)
            cameraLauncher.launch(photoUri)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (currentPhotoUri != null) {
                // Display selected photo
                AsyncImage(
                    model = currentPhotoUri,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Remove photo button
                IconButton(
                    onClick = onPhotoRemoved,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove Photo",
                        tint = Color.White
                    )
                }
            } else {
                // Photo placeholder
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Add Profile Photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showBottomSheet = true }
                    ) {
                        Text("Select Photo")
                    }
                }
            }
        }
    }
    
    // Photo selection bottom sheet
    if (showBottomSheet) {
        PhotoSelectionBottomSheet(
            onCameraSelected = {
                showBottomSheet = false
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val photoUri = createImageUri(context)
                        cameraLauncher.launch(photoUri)
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            onGallerySelected = {
                showBottomSheet = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

@Composable
fun PhotoSelectionBottomSheet(
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Photo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhotoOptionCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    onClick = onCameraSelected
                )
                
                PhotoOptionCard(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Gallery",
                    onClick = onGallerySelected
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhotoOptionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val imageFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "profile_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
```

### 2. Photo Processing & Upload Manager

```kotlin
// data/photo/PhotoManager.kt
@Singleton
class PhotoManager @Inject constructor(
    private val storage: FirebaseStorage,
    private val context: Context
) {
    
    suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
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
    
    private suspend fun compressImage(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        
        // Resize to max 800x800 while maintaining aspect ratio
        val resizedBitmap = resizeBitmap(originalBitmap, 800, 800)
        
        // Compress to JPEG with 80% quality
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        
        outputStream.toByteArray()
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            val newW = minOf(maxWidth, width)
            val newH = (newW / aspectRatio).toInt()
            newW to newH
        } else {
            // Portrait or square
            val newH = minOf(maxHeight, height)
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
    
    fun validatePhoto(uri: Uri): PhotoValidationResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            
            val fileSize = getFileSize(uri)
            
            when {
                fileSize > 10 * 1024 * 1024 -> // 10MB limit
                    PhotoValidationResult.Error("File size too large (max 10MB)")
                options.outWidth < 200 || options.outHeight < 200 ->
                    PhotoValidationResult.Error("Image resolution too low (min 200x200)")
                options.outMimeType !in listOf("image/jpeg", "image/png") ->
                    PhotoValidationResult.Error("Invalid file format (JPEG/PNG only)")
                else -> PhotoValidationResult.Valid
            }
        } catch (e: Exception) {
            PhotoValidationResult.Error("Invalid image file")
        }
    }
    
    private fun getFileSize(uri: Uri): Long {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L
    }
}

sealed class PhotoValidationResult {
    object Valid : PhotoValidationResult()
    data class Error(val message: String) : PhotoValidationResult()
}
```

### 3. Photo Integration in Registration Form

```kotlin
// presentation/ui/registration/PersonalDetailsScreen.kt
@Composable
fun PersonalDetailsScreen(
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = 0.25f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Personal Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Photo capture section
        PhotoCaptureComponent(
            currentPhotoUri = uiState.profilePhotoUri,
            onPhotoSelected = { uri ->
                viewModel.validateAndSetPhoto(uri)
            },
            onPhotoRemoved = {
                viewModel.removePhoto()
            }
        )
        
        // Show photo upload progress
        if (uiState.photoUploadProgress > 0f && uiState.photoUploadProgress < 1f) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = uiState.photoUploadProgress,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Uploading photo... ${(uiState.photoUploadProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Show photo validation error
        uiState.photoError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Rest of the form fields...
        PersonalDetailsForm(
            formState = uiState.personalDetails,
            onFormChange = viewModel::updatePersonalDetails
        )
    }
}
```

### 4. ViewModel with Photo Management

```kotlin
// presentation/viewmodel/RegistrationViewModel.kt
@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val photoManager: PhotoManager,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()
    
    fun validateAndSetPhoto(uri: Uri) {
        viewModelScope.launch {
            val validationResult = photoManager.validatePhoto(uri)
            
            when (validationResult) {
                is PhotoValidationResult.Valid -> {
                    _uiState.update { 
                        it.copy(
                            profilePhotoUri = uri,
                            photoError = null
                        )
                    }
                    uploadPhoto(uri)
                }
                is PhotoValidationResult.Error -> {
                    _uiState.update { 
                        it.copy(photoError = validationResult.message)
                    }
                }
            }
        }
    }
    
    private fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            val userId = getCurrentUserId() // Get current user ID
            
            photoManager.uploadProfilePhoto(
                userId = userId,
                photoUri = uri,
                onProgress = { progress ->
                    _uiState.update { 
                        it.copy(photoUploadProgress = progress)
                    }
                }
            ).fold(
                onSuccess = { downloadUrl ->
                    _uiState.update { 
                        it.copy(
                            profilePhotoUrl = downloadUrl,
                            photoUploadProgress = 1f,
                            photoError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            photoError = "Upload failed: ${error.message}",
                            photoUploadProgress = 0f
                        )
                    }
                }
            )
        }
    }
    
    fun removePhoto() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            
            photoManager.deleteProfilePhoto(userId)
            
            _uiState.update { 
                it.copy(
                    profilePhotoUri = null,
                    profilePhotoUrl = null,
                    photoUploadProgress = 0f,
                    photoError = null
                )
            }
        }
    }
}

data class RegistrationUiState(
    val profilePhotoUri: Uri? = null,
    val profilePhotoUrl: String? = null,
    val photoUploadProgress: Float = 0f,
    val photoError: String? = null,
    val personalDetails: PersonalDetailsState = PersonalDetailsState(),
    // ... other fields
)
```

### 5. PDF Integration with Photo

```kotlin
// data/pdf/RegistrationPDFGenerator.kt (Updated)
class RegistrationPDFGenerator {
    fun generateRegistrationForm(
        user: User,
        tournament: Tournament,
        registration: Registration
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        
        // Add tournament header
        val title = Paragraph("TOURNAMENT REGISTRATION FORM")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(18f)
            .setBold()
        document.add(title)
        
        // Create main table with photo
        val mainTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
        mainTable.setWidth(UnitValue.createPercentValue(100f))
        
        // Left column - Player details
        val detailsCell = Cell()
        
        // Player details table
        val playerTable = Table(2)
        playerTable.addCell("Full Name").addCell(user.fullName)
        playerTable.addCell("Date of Birth").addCell(user.dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        playerTable.addCell("Gender").addCell(user.gender.name)
        playerTable.addCell("Email").addCell(user.email)
        playerTable.addCell("Phone").addCell(user.phone)
        user.clubName?.let { 
            playerTable.addCell("Club/Academy").addCell(it)
        }
        
        detailsCell.add(playerTable)
        mainTable.addCell(detailsCell)
        
        // Right column - Photo
        val photoCell = Cell()
        user.profilePhotoUrl?.let { photoUrl ->
            try {
                val imageData = downloadImage(photoUrl)
                val imageData = ImageDataFactory.create(imageData)
                val image = Image(imageData)
                    .setWidth(120f)
                    .setHeight(150f)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                
                photoCell.add(image)
            } catch (e: Exception) {
                // Add placeholder if photo fails to load
                photoCell.add(Paragraph("Photo\nNot Available")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10f))
            }
        } ?: run {
            // Add placeholder for missing photo
            photoCell.add(Paragraph("Photo\nNot Provided")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f))
        }
        
        mainTable.addCell(photoCell)
        document.add(mainTable)
        
        // Rest of the PDF content...
        // Selected events, payment details, etc.
        
        document.close()
        return outputStream.toByteArray()
    }
    
    private suspend fun downloadImage(url: String): ByteArray {
        // Download image from Firebase Storage URL
        return withContext(Dispatchers.IO) {
            URL(url).readBytes()
        }
    }
}
```

### 6. Required Permissions & FileProvider Setup

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<application>
    <!-- FileProvider for camera photos -->
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>
```

```xml
<!-- res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="pictures" path="Pictures" />
</paths>
```

This comprehensive photo management system provides:
- ✅ Camera and gallery photo selection
- ✅ Photo validation and compression
- ✅ Firebase Storage upload with progress
- ✅ Photo integration in PDF forms
- ✅ Error handling and user feedback
- ✅ Proper permissions and file handling