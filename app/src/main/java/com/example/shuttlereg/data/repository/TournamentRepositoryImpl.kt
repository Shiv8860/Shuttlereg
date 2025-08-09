package com.example.shuttlereg.data.repository

import com.example.shuttlereg.data.local.dao.TournamentDao
import com.example.shuttlereg.data.local.entity.TournamentEntity
import com.example.shuttlereg.domain.model.ContactInfo
import com.example.shuttlereg.domain.model.EventCategory
import com.example.shuttlereg.domain.model.Tournament
import com.example.shuttlereg.domain.repository.TournamentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TournamentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val tournamentDao: TournamentDao
) : TournamentRepository {

    override suspend fun getTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getAllTournaments().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override suspend fun getTournamentById(tournamentId: String): Result<Tournament> {
        return try {
            // Try local first
            val localTournament = tournamentDao.getTournamentById(tournamentId)
            if (localTournament != null) {
                Result.success(mapEntityToDomain(localTournament))
            } else {
                // Fetch from remote
                val document = firestore.collection("tournaments").document(tournamentId).get().await()
                if (document.exists()) {
                    val tournament = mapDocumentToDomain(document.data ?: emptyMap(), tournamentId)
                    // Cache locally
                    tournamentDao.insertTournament(mapDomainToEntity(tournament))
                    Result.success(tournament)
                } else {
                    Result.failure(Exception("Tournament not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveTournaments(): Flow<List<Tournament>> = callbackFlow {
        val listener = firestore.collection("tournaments")
            .whereEqualTo("isActive", true)
            .whereGreaterThan("registrationDeadline", System.currentTimeMillis())
            .orderBy("registrationDeadline")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val tournaments = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        mapDocumentToDomain(doc.data ?: emptyMap(), doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                // Cache locally
                try {
                    val entities = tournaments.map { mapDomainToEntity(it) }
                    tournamentDao.insertTournaments(entities)
                } catch (e: Exception) {
                    // Ignore caching errors
                }
                
                trySend(tournaments)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun searchTournaments(query: String): Flow<List<Tournament>> = callbackFlow {
        val listener = firestore.collection("tournaments")
            .whereEqualTo("isActive", true)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val tournaments = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val tournament = mapDocumentToDomain(doc.data ?: emptyMap(), doc.id)
                        // Simple text search
                        if (tournament.name.contains(query, ignoreCase = true) ||
                            tournament.description.contains(query, ignoreCase = true) ||
                            tournament.venue.contains(query, ignoreCase = true)) {
                            tournament
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(tournaments)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun refreshTournaments(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("tournaments")
                .whereEqualTo("isActive", true)
                .get().await()
            
            val tournaments = snapshot.documents.mapNotNull { doc ->
                try {
                    mapDocumentToDomain(doc.data ?: emptyMap(), doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            
            // Clear and update local cache
            tournamentDao.clearAllTournaments()
            val entities = tournaments.map { mapDomainToEntity(it) }
            tournamentDao.insertTournaments(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDocumentToDomain(data: Map<String, Any>, id: String): Tournament {
        return Tournament(
            id = id,
            name = data["name"] as? String ?: "",
            description = data["description"] as? String ?: "",
            startDate = (data["startDate"] as? String)?.let {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            endDate = (data["endDate"] as? String)?.let {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            venue = data["venue"] as? String ?: "",
            registrationDeadline = (data["registrationDeadline"] as? Long)?.let {
                try {
                    LocalDateTime.ofEpochSecond(it / 1000, ((it % 1000) * 1000000).toInt(), java.time.ZoneOffset.UTC)
                } catch (e: Exception) {
                    null
                }
            },
            availableEvents = (data["availableEvents"] as? List<String>)?.mapNotNull { eventName ->
                try {
                    EventCategory.valueOf(eventName)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList(),
            eventPrices = (data["eventPrices"] as? Map<String, Any>)?.mapValues { (_, value) ->
                when (value) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            } ?: emptyMap(),
            rules = data["rules"] as? String ?: "",
            contactInfo = (data["contactInfo"] as? Map<String, Any>)?.let { contactData ->
                ContactInfo(
                    organizerName = contactData["organizerName"] as? String ?: "",
                    email = contactData["email"] as? String ?: "",
                    phone = contactData["phone"] as? String ?: "",
                    website = contactData["website"] as? String,
                    address = contactData["address"] as? String ?: ""
                )
            } ?: ContactInfo(),
            isActive = data["isActive"] as? Boolean ?: true,
            maxParticipants = (data["maxParticipants"] as? Number)?.toInt() ?: 0,
            currentParticipants = (data["currentParticipants"] as? Number)?.toInt() ?: 0,
            bannerImageUrl = data["bannerImageUrl"] as? String,
            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
            updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis()
        )
    }

    private fun mapDomainToEntity(tournament: Tournament): TournamentEntity {
        return TournamentEntity(
            id = tournament.id,
            name = tournament.name,
            description = tournament.description,
            startDate = tournament.startDate?.toString(),
            endDate = tournament.endDate?.toString(),
            venue = tournament.venue,
            registrationDeadline = tournament.registrationDeadline?.toEpochSecond(java.time.ZoneOffset.UTC)?.times(1000),
            availableEvents = tournament.availableEvents.map { it.name },
            eventPrices = tournament.eventPrices,
            rules = tournament.rules,
            contactInfo = mapOf(
                "organizerName" to tournament.contactInfo.organizerName,
                "email" to tournament.contactInfo.email,
                "phone" to tournament.contactInfo.phone,
                "website" to tournament.contactInfo.website,
                "address" to tournament.contactInfo.address
            ),
            isActive = tournament.isActive,
            maxParticipants = tournament.maxParticipants,
            currentParticipants = tournament.currentParticipants,
            bannerImageUrl = tournament.bannerImageUrl,
            createdAt = tournament.createdAt,
            updatedAt = tournament.updatedAt
        )
    }

    private fun mapEntityToDomain(entity: TournamentEntity): Tournament {
        return Tournament(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            startDate = entity.startDate?.let {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            endDate = entity.endDate?.let {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            venue = entity.venue,
            registrationDeadline = entity.registrationDeadline?.let {
                try {
                    LocalDateTime.ofEpochSecond(it / 1000, ((it % 1000) * 1000000).toInt(), java.time.ZoneOffset.UTC)
                } catch (e: Exception) {
                    null
                }
            },
            availableEvents = entity.availableEvents.mapNotNull { eventName ->
                try {
                    EventCategory.valueOf(eventName)
                } catch (e: Exception) {
                    null
                }
            },
            eventPrices = entity.eventPrices,
            rules = entity.rules,
            contactInfo = ContactInfo(
                organizerName = entity.contactInfo["organizerName"] as? String ?: "",
                email = entity.contactInfo["email"] as? String ?: "",
                phone = entity.contactInfo["phone"] as? String ?: "",
                website = entity.contactInfo["website"] as? String,
                address = entity.contactInfo["address"] as? String ?: ""
            ),
            isActive = entity.isActive,
            maxParticipants = entity.maxParticipants,
            currentParticipants = entity.currentParticipants,
            bannerImageUrl = entity.bannerImageUrl,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}