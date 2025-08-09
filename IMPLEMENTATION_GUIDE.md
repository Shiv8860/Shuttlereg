# ShuttleReg Implementation Guide

## 🚀 Quick Start Implementation

### 1. Age-Based Event Selection Logic

```kotlin
// domain/model/EventCategory.kt
enum class EventCategory(val displayName: String, val ageLimit: String) {
    U9("Under 9", "born on or after 01.01.2016"),
    U11("Under 11", "born on or after 01.01.2014"),
    U13("Under 13", "born on or after 01.01.2012"),
    U15("Under 15", "born on or after 01.01.2010"),
    U17("Under 17", "born on or after 01.01.2008"),
    U19("Under 19", "born on or after 01.01.2006"),
    MENS_OPEN("Men's Open", "18+ years"),
    WOMENS_OPEN("Women's Open", "18+ years")
}

enum class EventType {
    SINGLES, DOUBLES, MIXED_DOUBLES
}

// domain/usecase/CalculateEligibleEventsUseCase.kt
class CalculateEligibleEventsUseCase {
    operator fun invoke(dateOfBirth: LocalDate, gender: Gender): List<EventCategory> {
        val birthYear = dateOfBirth.year
        val eligibleEvents = mutableListOf<EventCategory>()
        
        // Age-based eligibility
        if (birthYear >= 2016) eligibleEvents.add(EventCategory.U9)
        if (birthYear >= 2014) eligibleEvents.add(EventCategory.U11)
        if (birthYear >= 2012) eligibleEvents.add(EventCategory.U13)
        if (birthYear >= 2010) eligibleEvents.add(EventCategory.U15)
        if (birthYear >= 2008) eligibleEvents.add(EventCategory.U17)
        if (birthYear >= 2006) eligibleEvents.add(EventCategory.U19)
        
        // Open categories for 18+
        if (birthYear <= 2006) {
            when (gender) {
                Gender.MALE -> eligibleEvents.add(EventCategory.MENS_OPEN)
                Gender.FEMALE -> eligibleEvents.add(EventCategory.WOMENS_OPEN)
            }
        }
        
        return eligibleEvents
    }
}
```

### 2. Registration Form with Validation

```kotlin
// presentation/ui/registration/RegistrationFormScreen.kt
@Composable
fun RegistrationFormScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    onNavigateToEventSelection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress Indicator
        LinearProgressIndicator(
            progress = 0.33f, // Step 1 of 3
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Personal Details Form
        PersonalDetailsForm(
            formState = uiState.personalDetails,
            onFormChange = viewModel::updatePersonalDetails,
            onDateOfBirthSelected = { dob ->
                viewModel.updateDateOfBirth(dob)
                viewModel.calculateEligibleEvents()
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Continue Button
        Button(
            onClick = {
                if (viewModel.validatePersonalDetails()) {
                    onNavigateToEventSelection()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isPersonalDetailsValid
        ) {
            Text("Continue to Event Selection")
        }
    }
}

@Composable
fun PersonalDetailsForm(
    formState: PersonalDetailsState,
    onFormChange: (PersonalDetailsState) -> Unit,
    onDateOfBirthSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = formState.fullName,
        onValueChange = { onFormChange(formState.copy(fullName = it)) },
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth(),
        isError = formState.fullNameError != null,
        supportingText = formState.fullNameError?.let { { Text(it) } }
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Date of Birth Picker
    OutlinedTextField(
        value = formState.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
        onValueChange = { },
        label = { Text("Date of Birth") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        enabled = false,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        }
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                onDateOfBirthSelected(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Gender Selection
    GenderSelectionRow(
        selectedGender = formState.gender,
        onGenderSelected = { onFormChange(formState.copy(gender = it)) }
    )
    
    // Contact Information
    ContactInformationSection(
        email = formState.email,
        phone = formState.phone,
        onEmailChange = { onFormChange(formState.copy(email = it)) },
        onPhoneChange = { onFormChange(formState.copy(phone = it)) }
    )
}
```

### 3. Event Selection with Partner Details

```kotlin
// presentation/ui/registration/EventSelectionScreen.kt
@Composable
fun EventSelectionScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    onNavigateToSummary: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Select Events",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Based on your age, you're eligible for:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(uiState.eligibleEvents) { eventCategory ->
            EventCategoryCard(
                category = eventCategory,
                selectedEvents = uiState.selectedEvents,
                onEventToggle = viewModel::toggleEvent,
                onPartnerInfoChange = viewModel::updatePartnerInfo
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateToSummary,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedEvents.isNotEmpty()
            ) {
                Text("Continue to Summary")
            }
        }
    }
}

@Composable
fun EventCategoryCard(
    category: EventCategory,
    selectedEvents: List<EventSelection>,
    onEventToggle: (EventCategory, EventType) -> Unit,
    onPartnerInfoChange: (EventCategory, EventType, PartnerInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = category.ageLimit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Singles Option
            EventTypeCheckbox(
                eventType = EventType.SINGLES,
                category = category,
                isSelected = selectedEvents.any { it.category == category && it.type == EventType.SINGLES },
                onToggle = onEventToggle
            )
            
            // Doubles Option
            val doublesSelected = selectedEvents.any { it.category == category && it.type == EventType.DOUBLES }
            EventTypeCheckbox(
                eventType = EventType.DOUBLES,
                category = category,
                isSelected = doublesSelected,
                onToggle = onEventToggle
            )
            
            if (doublesSelected) {
                PartnerInfoSection(
                    category = category,
                    eventType = EventType.DOUBLES,
                    onPartnerInfoChange = onPartnerInfoChange
                )
            }
            
            // Mixed Doubles Option
            val mixedDoublesSelected = selectedEvents.any { it.category == category && it.type == EventType.MIXED_DOUBLES }
            EventTypeCheckbox(
                eventType = EventType.MIXED_DOUBLES,
                category = category,
                isSelected = mixedDoublesSelected,
                onToggle = onEventToggle
            )
            
            if (mixedDoublesSelected) {
                PartnerInfoSection(
                    category = category,
                    eventType = EventType.MIXED_DOUBLES,
                    onPartnerInfoChange = onPartnerInfoChange
                )
            }
        }
    }
}
```

### 4. Payment Integration with Razorpay

```kotlin
// data/payment/RazorpayPaymentManager.kt
class RazorpayPaymentManager(
    private val context: Context
) {
    fun initiatePayment(
        amount: Double,
        orderId: String,
        userEmail: String,
        userPhone: String,
        onSuccess: (PaymentData) -> Unit,
        onError: (Int, String) -> Unit
    ) {
        val razorpay = Razorpay(context as Activity)
        
        val options = JSONObject().apply {
            put("name", "ShuttleReg Tournament")
            put("description", "Tournament Registration Fee")
            put("order_id", orderId)
            put("currency", "INR")
            put("amount", (amount * 100).toInt()) // Amount in paise
            
            val prefill = JSONObject().apply {
                put("email", userEmail)
                put("contact", userPhone)
            }
            put("prefill", prefill)
            
            val theme = JSONObject().apply {
                put("color", "#4CAF50") // Badminton green
            }
            put("theme", theme)
        }
        
        razorpay.setPaymentResultListener(object : PaymentResultListener {
            override fun onPaymentSuccess(razorpayPaymentID: String?) {
                razorpayPaymentID?.let { paymentId ->
                    onSuccess(PaymentData(paymentId, orderId, amount))
                }
            }
            
            override fun onPaymentError(code: Int, response: String?) {
                onError(code, response ?: "Payment failed")
            }
        })
        
        razorpay.open(options)
    }
}

// presentation/ui/payment/PaymentScreen.kt
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = hiltViewModel(),
    onPaymentSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(uiState.paymentStatus) {
        when (uiState.paymentStatus) {
            PaymentStatus.SUCCESS -> onPaymentSuccess()
            PaymentStatus.FAILED -> {
                // Show error message
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PaymentSummaryCard(
            selectedEvents = uiState.selectedEvents,
            totalAmount = uiState.totalAmount
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                viewModel.initiatePayment(context)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.paymentStatus != PaymentStatus.PROCESSING
        ) {
            if (uiState.paymentStatus == PaymentStatus.PROCESSING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Pay ₹${uiState.totalAmount}")
            }
        }
    }
}
```

### 5. PDF Generation

```kotlin
// data/pdf/RegistrationPDFGenerator.kt
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
        
        // Tournament details
        document.add(Paragraph("Tournament: ${tournament.name}"))
        document.add(Paragraph("Venue: ${tournament.venue}"))
        document.add(Paragraph("Date: ${tournament.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"))
        
        document.add(Paragraph("\n"))
        
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
        
        document.add(playerTable)
        
        document.add(Paragraph("\n"))
        
        // Selected events
        document.add(Paragraph("SELECTED EVENTS").setBold())
        registration.selectedEvents.forEach { event ->
            val eventText = "${event.category.displayName} - ${event.type.name}"
            document.add(Paragraph("• $eventText"))
            
            event.partnerInfo?.let { partner ->
                document.add(Paragraph("  Partner: ${partner.name} (${partner.phone})"))
            }
        }
        
        document.add(Paragraph("\n"))
        
        // Payment details
        document.add(Paragraph("PAYMENT DETAILS").setBold())
        document.add(Paragraph("Total Amount: ₹${registration.totalAmount}"))
        document.add(Paragraph("Payment ID: ${registration.paymentId}"))
        document.add(Paragraph("Registration Date: ${registration.registrationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}"))
        
        // Add QR code for verification
        val qrCodeData = "REG:${registration.id}:${user.id}:${tournament.id}"
        val qrCode = generateQRCode(qrCodeData)
        // Add QR code to document
        
        document.close()
        return outputStream.toByteArray()
    }
    
    private fun generateQRCode(data: String): ByteArray {
        // QR code generation logic
        return byteArrayOf()
    }
}
```

### 6. Firebase Repository Implementation

```kotlin
// data/repository/TournamentRepositoryImpl.kt
@Singleton
class TournamentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val tournamentDao: TournamentDao
) : TournamentRepository {
    
    override suspend fun getTournaments(): Flow<List<Tournament>> = flow {
        try {
            // Emit cached data first
            emit(tournamentDao.getAllTournaments().map { it.toDomain() })
            
            // Fetch from remote
            val snapshot = firestore.collection("tournaments")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val tournaments = snapshot.documents.mapNotNull { doc ->
                doc.toObject<TournamentDto>()?.toDomain()
            }
            
            // Update cache
            tournamentDao.insertTournaments(tournaments.map { it.toEntity() })
            
            // Emit fresh data
            emit(tournaments)
        } catch (e: Exception) {
            // Emit cached data on error
            emit(tournamentDao.getAllTournaments().map { it.toDomain() })
        }
    }
    
    override suspend fun registerForTournament(registration: Registration): Result<String> {
        return try {
            val registrationDto = registration.toDto()
            val docRef = firestore.collection("registrations").add(registrationDto).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 7. Push Notifications

```kotlin
// data/notification/FirebaseMessagingService.kt
class ShuttleRegMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val notificationType = remoteMessage.data["type"]
        val tournamentId = remoteMessage.data["tournamentId"]
        
        when (notificationType) {
            "tournament_announcement" -> handleTournamentAnnouncement(remoteMessage)
            "registration_confirmation" -> handleRegistrationConfirmation(remoteMessage)
            "draw_released" -> handleDrawRelease(remoteMessage, tournamentId)
            "match_update" -> handleMatchUpdate(remoteMessage)
            else -> handleGenericNotification(remoteMessage)
        }
    }
    
    private fun handleTournamentAnnouncement(message: RemoteMessage) {
        showNotification(
            title = message.notification?.title ?: "Tournament Update",
            body = message.notification?.body ?: "",
            channelId = "tournament_announcements"
        )
    }
    
    private fun showNotification(title: String, body: String, channelId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_badminton)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

## 🔧 Development Setup Instructions

1. **Firebase Setup**:
   - Replace `google-services.json` with your Firebase project config
   - Enable Authentication, Firestore, Storage, and Cloud Messaging
   - Set up Firestore security rules

2. **Razorpay Setup**:
   - Get API keys from Razorpay dashboard
   - Add keys to `local.properties` file
   - Test with Razorpay test mode

3. **Build Configuration**:
   ```kotlin
   // In app/build.gradle.kts
   android {
       buildConfigField("String", "RAZORPAY_KEY", "\"${project.findProperty("RAZORPAY_KEY")}\"")
   }
   ```

4. **Permissions in AndroidManifest.xml**:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.CAMERA" />
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

This implementation guide provides the core structure and key components needed to build your badminton tournament app. Each section can be expanded based on specific requirements and additional features.