package com.example.shuttlereg.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttlereg.domain.model.Tournament
import com.example.shuttlereg.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentUiState())
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    init {
        loadActiveTournaments()
    }

    fun loadActiveTournaments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            tournamentRepository.getActiveTournaments()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load tournaments"
                    )
                }
                .collect { tournaments ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tournaments = tournaments,
                        errorMessage = null
                    )
                }
        }
    }

    fun selectTournament(tournament: Tournament) {
        _uiState.value = _uiState.value.copy(selectedTournament = tournament)
    }

    fun searchTournaments(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        viewModelScope.launch {
            if (query.isBlank()) {
                loadActiveTournaments()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                tournamentRepository.searchTournaments(query)
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Search failed"
                        )
                    }
                    .collect { tournaments ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tournaments = tournaments,
                            errorMessage = null
                        )
                    }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshTournaments() {
        loadActiveTournaments()
    }
}

data class TournamentUiState(
    val isLoading: Boolean = false,
    val tournaments: List<Tournament> = emptyList(),
    val selectedTournament: Tournament? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
)