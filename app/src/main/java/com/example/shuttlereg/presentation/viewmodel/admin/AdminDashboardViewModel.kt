package com.example.shuttlereg.presentation.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttlereg.domain.model.*
import com.example.shuttlereg.domain.repository.RegistrationRepository
import com.example.shuttlereg.domain.repository.TournamentRepository
import com.example.shuttlereg.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val registrationRepository: RegistrationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Load active tournaments (for demo, we'll use the first active tournament)
                tournamentRepository.getActiveTournaments()
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load tournament data"
                        )
                    }
                    .collect { tournaments ->
                        val tournament = tournaments.firstOrNull()
                        
                        if (tournament != null) {
                            loadTournamentStats(tournament)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                tournament = null,
                                stats = AdminStats(),
                                upcomingMatches = emptyList(),
                                notifications = generateMockNotifications()
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun loadTournamentStats(tournament: Tournament) {
        viewModelScope.launch {
            try {
                // Load tournament registrations to calculate stats
                registrationRepository.getTournamentRegistrations(tournament.id)
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load registration data"
                        )
                    }
                    .collect { registrations ->
                        val stats = calculateStats(registrations)
                        val matches = generateMatchesFromRegistrations(registrations)
                        val upcomingMatches = matches.filter { 
                            it.status == MatchStatus.SCHEDULED && 
                            (it.scheduledDateTime?.isAfter(LocalDateTime.now()) ?: true)
                        }.take(5)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tournament = tournament,
                            stats = stats,
                            upcomingMatches = upcomingMatches,
                            notifications = generateMockNotifications()
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error loading tournament stats"
                )
            }
        }
    }

    private fun calculateStats(registrations: List<Registration>): AdminStats {
        val totalPlayers = registrations.size
        val totalRevenue = registrations.filter { 
            it.paymentStatus == PaymentStatus.SUCCESS 
        }.sumOf { it.totalAmount }
        
        val pendingPayments = registrations.count { 
            it.paymentStatus == PaymentStatus.PENDING 
        }
        
        val totalMatches = registrations.sumOf { it.selectedEvents.size }
        
        return AdminStats(
            totalPlayers = totalPlayers,
            totalMatches = totalMatches,
            totalCourts = 4, // Default court count
            totalRevenue = totalRevenue,
            pendingPayments = pendingPayments,
            completedMatches = (totalMatches * 0.3).toInt(), // Mock: 30% completed
            ongoingMatches = (totalMatches * 0.1).toInt() // Mock: 10% ongoing
        )
    }

    private fun generateMatchesFromRegistrations(registrations: List<Registration>): List<Match> {
        val matches = mutableListOf<Match>()
        
        registrations.forEach { registration ->
            registration.selectedEvents.forEach { eventSelection ->
                val match = Match(
                    id = "admin_match_${registration.id}_${eventSelection.category.name}_${eventSelection.type.name}",
                    tournamentId = registration.tournamentId,
                    tournamentName = _uiState.value.tournament?.name ?: "Tournament",
                    registrationId = registration.id,
                    userId = registration.userId,
                    eventCategory = eventSelection.category,
                    eventType = eventSelection.type,
                    partnerName = eventSelection.partnerInfo?.name,
                    partnerId = eventSelection.partnerInfo?.id,
                    opponentName = generateMockOpponentName(),
                    scheduledDateTime = generateMockDateTime(),
                    courtNumber = (1..4).random(),
                    status = generateMockStatus(),
                    round = listOf("First Round", "Second Round", "Quarterfinal", "Semifinal", "Final").random()
                )
                matches.add(match)
            }
        }
        
        return matches.sortedBy { it.scheduledDateTime }
    }

    private fun generateMockOpponentName(): String {
        val names = listOf(
            "Rahul Sharma", "Priya Patel", "Amit Kumar", "Ananya Gupta",
            "Karan Mehta", "Sneha Singh", "Vivek Verma", "Meera Joshi",
            "Arjun Reddy", "Kavya Nair", "Rohan Das", "Divya Iyer"
        )
        return names.random()
    }

    private fun generateMockDateTime(): LocalDateTime {
        val now = LocalDateTime.now()
        val daysOffset = (0..7).random().toLong()
        val hoursOffset = (8..18).random().toLong()
        return now.plusDays(daysOffset).withHour(hoursOffset.toInt()).withMinute(listOf(0, 15, 30, 45).random())
    }

    private fun generateMockStatus(): MatchStatus {
        return when ((0..10).random()) {
            in 0..6 -> MatchStatus.SCHEDULED
            in 7..8 -> MatchStatus.COMPLETED
            9 -> MatchStatus.IN_PROGRESS
            else -> MatchStatus.CANCELLED
        }
    }

    private fun generateMockNotifications(): List<AdminNotification> {
        return listOf(
            AdminNotification(
                id = "notif_1",
                type = AdminNotificationType.PAYMENT_PENDING,
                message = "2 payments pending approval"
            ),
            AdminNotification(
                id = "notif_2",
                type = AdminNotificationType.PLAYER_REQUEST,
                message = "1 player requested category change"
            ),
            AdminNotification(
                id = "notif_3",
                type = AdminNotificationType.SYSTEM_ALERT,
                message = "Court 3 maintenance scheduled"
            )
        )
    }

    fun refreshData() {
        loadDashboardData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val tournament: Tournament? = null,
    val stats: AdminStats = AdminStats(),
    val upcomingMatches: List<Match> = emptyList(),
    val notifications: List<AdminNotification> = emptyList(),
    val errorMessage: String? = null
)