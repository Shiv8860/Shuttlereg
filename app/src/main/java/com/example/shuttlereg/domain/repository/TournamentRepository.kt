package com.example.shuttlereg.domain.repository

import com.example.shuttlereg.domain.model.Tournament
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {
    suspend fun getTournaments(): Flow<List<Tournament>>
    suspend fun getTournamentById(tournamentId: String): Result<Tournament>
    suspend fun getActiveTournaments(): Flow<List<Tournament>>
    suspend fun searchTournaments(query: String): Flow<List<Tournament>>
    suspend fun refreshTournaments(): Result<Unit>
}