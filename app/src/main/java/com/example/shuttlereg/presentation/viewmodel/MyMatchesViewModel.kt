package com.example.shuttlereg.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttlereg.domain.model.*
import com.example.shuttlereg.domain.repository.UserRepository
import com.example.shuttlereg.domain.repository.RegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MyMatchesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val registrationRepository: RegistrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyMatchesUiState())
    val uiState: StateFlow<MyMatchesUiState> = _uiState.asStateFlow()

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val user = userRepository.getCurrentUserSync()
            if (user != null) {
                // Load user's registrations and convert to matches
                registrationRepository.getUserRegistrations(user.id)
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load matches"
                        )
                    }
                    .collect { registrations ->
                        val matches = generateMatchesFromRegistrations(registrations, user.id)
                        val now = LocalDateTime.now()
                        
                        val upcomingMatches = matches.filter { match ->
                            match.status == MatchStatus.SCHEDULED && 
                            (match.scheduledDateTime?.isAfter(now) ?: true)
                        }
                        
                        val pastMatches = matches.filter { match ->
                            match.status == MatchStatus.COMPLETED ||
                            (match.status == MatchStatus.SCHEDULED && 
                             match.scheduledDateTime?.isBefore(now) == true)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            allMatches = matches,
                            upcomingMatches = upcomingMatches,
                            pastMatches = pastMatches,
                            errorMessage = null
                        )
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "User not authenticated"
                )
            }
        }
    }

    fun refreshMatches() {
        loadMatches()
    }

    private fun generateMatchesFromRegistrations(
        registrations: List<Registration>,
        userId: String
    ): List<Match> {
        val matches = mutableListOf<Match>()
        
        registrations.forEach { registration ->
            registration.selectedEvents.forEach { eventSelection ->
                // Generate a mock match for each event registration
                // In a real app, this would come from a matches collection
                val match = Match(
                    id = "match_${registration.id}_${eventSelection.category.name}_${eventSelection.type.name}",
                    tournamentId = registration.tournamentId,
                    tournamentName = "Tournament", // Would be fetched from tournament data
                    registrationId = registration.id,
                    userId = userId,
                    eventCategory = eventSelection.category,
                    eventType = eventSelection.type,
                    partnerName = eventSelection.partnerInfo?.name,
                    partnerId = eventSelection.partnerInfo?.id,
                    opponentName = generateMockOpponentName(),
                    scheduledDateTime = generateMockDateTime(),
                    courtNumber = (1..4).random(),
                    status = generateMockStatus(),
                    finalScore = if ((0..10).random() < 3) generateMockScore() else null,
                    result = if ((0..10).random() < 3) MatchResult.values().random() else null,
                    round = listOf("First Round", "Second Round", "Quarterfinal", "Semifinal", "Final").random()
                )
                matches.add(match)
            }
        }
        
        return matches.sortedByDescending { it.scheduledDateTime }
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
        val daysOffset = (-5..10).random().toLong()
        val hoursOffset = (8..18).random().toLong()
        return now.plusDays(daysOffset).withHour(hoursOffset.toInt()).withMinute(listOf(0, 15, 30, 45).random())
    }

    private fun generateMockStatus(): MatchStatus {
        return when ((0..10).random()) {
            in 0..5 -> MatchStatus.SCHEDULED
            in 6..7 -> MatchStatus.COMPLETED
            8 -> MatchStatus.IN_PROGRESS
            else -> MatchStatus.CANCELLED
        }
    }

    private fun generateMockScore(): String {
        val game1Player = (15..21).random()
        val game1Opponent = (10..21).random()
        val game2Player = (15..21).random()
        val game2Opponent = (10..21).random()
        
        return if ((0..1).random() == 0) {
            "$game1Player-$game1Opponent, $game2Player-$game2Opponent"
        } else {
            val game3Player = (15..21).random()
            val game3Opponent = (10..21).random()
            "$game1Player-$game1Opponent, $game2Player-$game2Opponent, $game3Player-$game3Opponent"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class MyMatchesUiState(
    val isLoading: Boolean = false,
    val allMatches: List<Match> = emptyList(),
    val upcomingMatches: List<Match> = emptyList(),
    val pastMatches: List<Match> = emptyList(),
    val errorMessage: String? = null
)