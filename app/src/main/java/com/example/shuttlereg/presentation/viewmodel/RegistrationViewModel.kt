package com.example.shuttlereg.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttlereg.domain.model.*
import com.example.shuttlereg.domain.repository.UserRepository
import com.example.shuttlereg.domain.repository.RegistrationRepository
import com.example.shuttlereg.domain.repository.TournamentRepository
import com.example.shuttlereg.domain.usecase.CalculateEligibleEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val calculateEligibleEventsUseCase: CalculateEligibleEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun initializeRegistration(tournamentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load tournament details
            tournamentRepository.getTournamentById(tournamentId).fold(
                onSuccess = { tournament ->
                    _uiState.value = _uiState.value.copy(
                        tournament = tournament,
                        isLoading = false
                    )
                    
                    // Check for existing draft
                    checkForDraftRegistration(tournamentId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load tournament"
                    )
                }
            )
        }
    }

    private suspend fun checkForDraftRegistration(tournamentId: String) {
        // Get current user ID
        val user = userRepository.getCurrentUserSync()
        if (user != null) {
            registrationRepository.getDraftRegistration(user.id, tournamentId).fold(
                onSuccess = { draftRegistration ->
                    draftRegistration?.let {
                        _uiState.value = _uiState.value.copy(
                            registration = it,
                            personalDetails = PersonalDetailsFormState(
                                fullName = user.fullName,
                                email = user.email,
                                phone = user.phone,
                                dateOfBirth = user.dateOfBirth,
                                gender = user.gender,
                                clubName = user.clubName ?: ""
                            )
                        )
                        calculateEligibleEvents()
                    }
                },
                onFailure = { /* No draft found, start fresh */ }
            )
        }
    }

    fun updatePersonalDetails(details: PersonalDetailsFormState) {
        _uiState.value = _uiState.value.copy(personalDetails = details)
        validatePersonalDetails()
    }

    fun updateDateOfBirth(dateOfBirth: LocalDate) {
        val updatedDetails = _uiState.value.personalDetails.copy(dateOfBirth = dateOfBirth)
        _uiState.value = _uiState.value.copy(personalDetails = updatedDetails)
        calculateEligibleEvents()
        validatePersonalDetails()
    }

    fun calculateEligibleEvents() {
        val dateOfBirth = _uiState.value.personalDetails.dateOfBirth
        val gender = _uiState.value.personalDetails.gender
        
        if (dateOfBirth != null) {
            val eligibleEvents = calculateEligibleEventsUseCase(dateOfBirth, gender)
            _uiState.value = _uiState.value.copy(eligibleEvents = eligibleEvents)
        }
    }

    fun selectEvent(eventSelection: EventSelection) {
        val currentSelections = _uiState.value.selectedEvents.toMutableList()
        val existingIndex = currentSelections.indexOfFirst { 
            it.category == eventSelection.category && it.type == eventSelection.type 
        }
        
        if (existingIndex >= 0) {
            currentSelections[existingIndex] = eventSelection
        } else {
            currentSelections.add(eventSelection)
        }
        
        _uiState.value = _uiState.value.copy(selectedEvents = currentSelections)
        calculateTotalAmount()
    }

    fun removeEvent(eventSelection: EventSelection) {
        val currentSelections = _uiState.value.selectedEvents.toMutableList()
        currentSelections.removeIf { 
            it.category == eventSelection.category && it.type == eventSelection.type 
        }
        
        _uiState.value = _uiState.value.copy(selectedEvents = currentSelections)
        calculateTotalAmount()
    }

    private fun calculateTotalAmount() {
        val tournament = _uiState.value.tournament ?: return
        val selectedEvents = _uiState.value.selectedEvents
        
        val totalAmount = selectedEvents.sumOf { event ->
            val eventKey = "${event.category.name}_${event.type.name}"
            tournament.eventPrices[eventKey] ?: 0.0
        }
        
        _uiState.value = _uiState.value.copy(totalAmount = totalAmount)
    }

    fun validatePersonalDetails(): Boolean {
        val details = _uiState.value.personalDetails
        val isValid = details.fullName.isNotBlank() &&
                details.email.isNotBlank() &&
                details.phone.isNotBlank() &&
                details.dateOfBirth != null &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(details.email).matches()
        
        _uiState.value = _uiState.value.copy(isPersonalDetailsValid = isValid)
        return isValid
    }

    fun validateEventSelection(): Boolean {
        val isValid = _uiState.value.selectedEvents.isNotEmpty()
        _uiState.value = _uiState.value.copy(isEventSelectionValid = isValid)
        return isValid
    }

    fun saveDraftRegistration() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val tournament = currentState.tournament ?: return@launch
            
            val user = userRepository.getCurrentUserSync()
            if (user != null) {
                val draftRegistration = Registration(
                    id = currentState.registration.id,
                    userId = user.id,
                    tournamentId = tournament.id,
                    selectedEvents = currentState.selectedEvents,
                    totalAmount = currentState.totalAmount,
                    status = RegistrationStatus.DRAFT
                )
                
                registrationRepository.saveDraftRegistration(draftRegistration)
            }
        }
    }

    fun submitRegistration() {
        if (!validatePersonalDetails() || !validateEventSelection()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val user = userRepository.getCurrentUserSync()
            if (user != null) {
                val tournament = _uiState.value.tournament!!
                val registration = Registration(
                    userId = user.id,
                    tournamentId = tournament.id,
                    selectedEvents = _uiState.value.selectedEvents,
                    totalAmount = _uiState.value.totalAmount,
                    status = RegistrationStatus.SUBMITTED
                )
                
                registrationRepository.createRegistration(registration).fold(
                    onSuccess = { registrationId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            registrationId = registrationId,
                            currentStep = RegistrationStep.PAYMENT
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Registration failed"
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Authentication error - user not logged in"
                )
            }
        }
    }

    fun processPayment(paymentData: PaymentData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val registrationId = _uiState.value.registrationId
            if (registrationId.isNotEmpty()) {
                registrationRepository.processPayment(registrationId, paymentData).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentStep = RegistrationStep.CONFIRMATION,
                            isRegistrationComplete = true
                        )
                        
                        // Generate PDF in background
                        generateRegistrationPDF()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Payment processing failed"
                        )
                    }
                )
            }
        }
    }

    private fun generateRegistrationPDF() {
        viewModelScope.launch {
            val registrationId = _uiState.value.registrationId
            if (registrationId.isNotEmpty()) {
                registrationRepository.generateRegistrationPDF(registrationId).fold(
                    onSuccess = { pdfUrl ->
                        _uiState.value = _uiState.value.copy(pdfUrl = pdfUrl)
                    },
                    onFailure = { /* Handle PDF generation error silently */ }
                )
            }
        }
    }

    fun nextStep() {
        when (_uiState.value.currentStep) {
            RegistrationStep.PERSONAL_DETAILS -> {
                if (validatePersonalDetails()) {
                    _uiState.value = _uiState.value.copy(currentStep = RegistrationStep.EVENT_SELECTION)
                }
            }
            RegistrationStep.EVENT_SELECTION -> {
                if (validateEventSelection()) {
                    submitRegistration()
                }
            }
            RegistrationStep.PAYMENT -> {
                // Payment is handled separately
            }
            RegistrationStep.CONFIRMATION -> {
                // Final step
            }
        }
    }

    fun previousStep() {
        when (_uiState.value.currentStep) {
            RegistrationStep.PERSONAL_DETAILS -> { /* First step */ }
            RegistrationStep.EVENT_SELECTION -> {
                _uiState.value = _uiState.value.copy(currentStep = RegistrationStep.PERSONAL_DETAILS)
            }
            RegistrationStep.PAYMENT -> {
                _uiState.value = _uiState.value.copy(currentStep = RegistrationStep.EVENT_SELECTION)
            }
            RegistrationStep.CONFIRMATION -> {
                _uiState.value = _uiState.value.copy(currentStep = RegistrationStep.PAYMENT)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class RegistrationUiState(
    val isLoading: Boolean = false,
    val tournament: Tournament? = null,
    val registration: Registration = Registration(),
    val personalDetails: PersonalDetailsFormState = PersonalDetailsFormState(),
    val eligibleEvents: List<EventCategory> = emptyList(),
    val selectedEvents: List<EventSelection> = emptyList(),
    val totalAmount: Double = 0.0,
    val currentStep: RegistrationStep = RegistrationStep.PERSONAL_DETAILS,
    val isPersonalDetailsValid: Boolean = false,
    val isEventSelectionValid: Boolean = false,
    val registrationId: String = "",
    val isRegistrationComplete: Boolean = false,
    val pdfUrl: String? = null,
    val errorMessage: String? = null
)

data class PersonalDetailsFormState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: LocalDate? = null,
    val gender: Gender = Gender.MALE,
    val clubName: String = ""
)

enum class RegistrationStep {
    PERSONAL_DETAILS,
    EVENT_SELECTION,
    PAYMENT,
    CONFIRMATION
}