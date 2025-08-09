package com.example.shuttlereg.data.local.dao

import androidx.room.*
import com.example.shuttlereg.data.local.entity.TournamentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    
    @Query("SELECT * FROM tournaments WHERE isActive = 1 ORDER BY startDate ASC")
    fun getAllActiveTournaments(): Flow<List<TournamentEntity>>
    
    @Query("SELECT * FROM tournaments ORDER BY startDate ASC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>
    
    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournamentById(tournamentId: String): TournamentEntity?
    
    @Query("SELECT * FROM tournaments WHERE name LIKE '%' || :query || '%' OR venue LIKE '%' || :query || '%'")
    fun searchTournaments(query: String): Flow<List<TournamentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<TournamentEntity>)
    
    @Update
    suspend fun updateTournament(tournament: TournamentEntity)
    
    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)
    
    @Query("DELETE FROM tournaments WHERE id = :tournamentId")
    suspend fun deleteTournamentById(tournamentId: String)
    
    @Query("DELETE FROM tournaments")
    suspend fun deleteAllTournaments()
    
    @Query("SELECT * FROM tournaments WHERE lastSynced < :timestamp")
    suspend fun getOutdatedTournaments(timestamp: Long): List<TournamentEntity>
    
    @Query("UPDATE tournaments SET lastSynced = :timestamp WHERE id = :tournamentId")
    suspend fun updateLastSynced(tournamentId: String, timestamp: Long)
}