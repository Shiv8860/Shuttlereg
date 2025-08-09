package com.example.shuttlereg.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shuttlereg.domain.model.*
import com.example.shuttlereg.presentation.viewmodel.RegistrationViewModel
import com.example.shuttlereg.presentation.viewmodel.RegistrationStep
import com.example.shuttlereg.presentation.viewmodel.PersonalDetailsFormState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    tournamentId: String,
    onNavigateBack: () -> Unit,
    onRegistrationComplete: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(tournamentId) {
        viewModel.initializeRegistration(tournamentId)
    }
    
    LaunchedEffect(uiState.isRegistrationComplete) {
        if (uiState.isRegistrationComplete) {
            onRegistrationComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = uiState.tournament?.name ?: "Registration",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Progress Indicator
        LinearProgressIndicator(
            progress = when (uiState.currentStep) {
                RegistrationStep.PERSONAL_DETAILS -> 0.25f
                RegistrationStep.EVENT_SELECTION -> 0.5f
                RegistrationStep.PAYMENT -> 0.75f
                RegistrationStep.CONFIRMATION -> 1.0f
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Step Content
        when (uiState.currentStep) {
            RegistrationStep.PERSONAL_DETAILS -> {
                PersonalDetailsStep(
                    formState = uiState.personalDetails,
                    isValid = uiState.isPersonalDetailsValid,
                    onFormChange = viewModel::updatePersonalDetails,
                    onDateOfBirthSelected = viewModel::updateDateOfBirth,
                    onNext = { viewModel.nextStep() },
                    isLoading = uiState.isLoading
                )
            }
            
            RegistrationStep.EVENT_SELECTION -> {
                EventSelectionStep(
                    tournament = uiState.tournament,
                    eligibleEvents = uiState.eligibleEvents,
                    selectedEvents = uiState.selectedEvents,
                    totalAmount = uiState.totalAmount,
                    isValid = uiState.isEventSelectionValid,
                    onEventSelected = viewModel::selectEvent,
                    onEventRemoved = viewModel::removeEvent,
                    onNext = { viewModel.nextStep() },
                    onPrevious = { viewModel.previousStep() },
                    isLoading = uiState.isLoading
                )
            }
            
            RegistrationStep.PAYMENT -> {
                PaymentStep(
                    tournament = uiState.tournament,
                    selectedEvents = uiState.selectedEvents,
                    totalAmount = uiState.totalAmount,
                    onPaymentComplete = viewModel::processPayment,
                    onPrevious = { viewModel.previousStep() },
                    isLoading = uiState.isLoading
                )
            }
            
            RegistrationStep.CONFIRMATION -> {
                ConfirmationStep(
                    tournament = uiState.tournament,
                    selectedEvents = uiState.selectedEvents,
                    totalAmount = uiState.totalAmount,
                    pdfUrl = uiState.pdfUrl,
                    onComplete = onRegistrationComplete
                )
            }
        }

        // Error Snackbar
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun PersonalDetailsStep(
    formState: PersonalDetailsFormState,
    isValid: Boolean,
    onFormChange: (PersonalDetailsFormState) -> Unit,
    onDateOfBirthSelected: (LocalDate) -> Unit,
    onNext: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Personal Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Please provide your personal information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full Name
        OutlinedTextField(
            value = formState.fullName,
            onValueChange = { onFormChange(formState.copy(fullName = it)) },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = formState.email,
            onValueChange = { onFormChange(formState.copy(email = it)) },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone
        OutlinedTextField(
            value = formState.phone,
            onValueChange = { onFormChange(formState.copy(phone = it)) },
            label = { Text("Phone Number") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date of Birth
        OutlinedTextField(
            value = formState.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
            onValueChange = { },
            label = { Text("Date of Birth") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date of Birth"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { 
                    // TODO: Show date picker
                    onDateOfBirthSelected(LocalDate.of(2000, 1, 1))
                }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pick Date"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Selection
        Text(
            text = "Gender",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Gender.values().forEach { gender ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = formState.gender == gender,
                            onClick = { onFormChange(formState.copy(gender = gender)) },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = formState.gender == gender,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = gender.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Club Name (Optional)
        OutlinedTextField(
            value = formState.clubName,
            onValueChange = { onFormChange(formState.copy(clubName = it)) },
            label = { Text("Club Name (Optional)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Club"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = isValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Continue to Event Selection")
        }
    }
}

@Composable
private fun EventSelectionStep(
    tournament: Tournament?,
    eligibleEvents: List<EventCategory>,
    selectedEvents: List<EventSelection>,
    totalAmount: Double,
    isValid: Boolean,
    onEventSelected: (EventSelection) -> Unit,
    onEventRemoved: (EventSelection) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = "Select Events",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Choose the events you want to participate in",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (eligibleEvents.isEmpty()) {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Please complete your personal details to see eligible events")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eligibleEvents) { category ->
                        EventCategoryCard(
                            category = category,
                            tournament = tournament,
                            selectedEvents = selectedEvents,
                            onEventSelected = onEventSelected,
                            onEventRemoved = onEventRemoved
                        )
                    }
                }
            }
        }

        // Bottom Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Previous")
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        enabled = isValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Proceed to Payment")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCategoryCard(
    category: EventCategory,
    tournament: Tournament?,
    selectedEvents: List<EventSelection>,
    onEventSelected: (EventSelection) -> Unit,
    onEventRemoved: (EventSelection) -> Unit
) {
    Card {
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

            // Event Types
            EventType.values().forEach { eventType ->
                val eventKey = "${category.name}_${eventType.name}"
                val price = tournament?.eventPrices?.get(eventKey) ?: 0.0
                val isSelected = selectedEvents.any { 
                    it.category == category && it.type == eventType 
                }

                if (price > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val eventSelection = EventSelection(
                                    category = category,
                                    type = eventType,
                                    price = price
                                )
                                if (checked) {
                                    onEventSelected(eventSelection)
                                } else {
                                    onEventRemoved(eventSelection)
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = eventType.name.replace("_", " ").lowercase()
                                    .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Text(
                            text = "₹${String.format("%.0f", price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStep(
    tournament: Tournament?,
    selectedEvents: List<EventSelection>,
    totalAmount: Double,
    onPaymentComplete: (PaymentData) -> Unit,
    onPrevious: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Payment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Review your registration and complete payment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Registration Summary
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Registration Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                selectedEvents.forEach { event ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${event.category.displayName} - ${event.type.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "₹${String.format("%.0f", event.price)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Payment Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Text("Previous")
            }

            Button(
                onClick = {
                    // TODO: Integrate Razorpay
                    val paymentData = PaymentData(
                        paymentId = "mock_payment_${System.currentTimeMillis()}",
                        orderId = "order_${System.currentTimeMillis()}",
                        amount = totalAmount
                    )
                    onPaymentComplete(paymentData)
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Pay Now")
            }
        }
    }
}

@Composable
private fun ConfirmationStep(
    tournament: Tournament?,
    selectedEvents: List<EventSelection>,
    totalAmount: Double,
    pdfUrl: String?,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Registration Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your registration for ${tournament?.name} has been confirmed.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (pdfUrl != null) {
            OutlinedButton(
                onClick = { /* TODO: Open PDF */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download Registration Form")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}