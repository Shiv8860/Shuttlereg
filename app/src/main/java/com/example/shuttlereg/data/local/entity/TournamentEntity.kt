package com.example.shuttlereg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.shuttlereg.domain.model.Tournament
import com.example.shuttlereg.domain.model.EventCategory
import com.example.shuttlereg.domain.model.ContactInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val startDate: Long?, // LocalDate as epoch day
    val endDate: Long?, // LocalDate as epoch day
    val venue: String,
    val registrationDeadline: Long?, // LocalDateTime as epoch seconds
    val availableEventsJson: String, // JSON string of List<EventCategory>
    val eventPricesJson: String, // JSON string of Map<String, Double>
    val rules: String,
    val contactInfoJson: String, // JSON string of ContactInfo
    val isActive: Boolean,
    val maxParticipants: Int,
    val currentParticipants: Int,
    val bannerImageUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSynced: Long = System.currentTimeMillis()
)

fun TournamentEntity.toDomain(): Tournament {
    val gson = Gson()
    
    val availableEvents = try {
        val type = object : TypeToken<List<String>>() {}.type
        val eventNames: List<String> = gson.fromJson(availableEventsJson, type)
        eventNames.mapNotNull { 
            try { 
                EventCategory.valueOf(it) 
            } catch (e: Exception) { 
                null 
            } 
        }
    } catch (e: Exception) {
        emptyList()
    }
    
    val eventPrices = try {
        val type = object : TypeToken<Map<String, Double>>() {}.type
        gson.fromJson<Map<String, Double>>(eventPricesJson, type) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }
    
    val contactInfo = try {
        gson.fromJson(contactInfoJson, ContactInfo::class.java) ?: ContactInfo()
    } catch (e: Exception) {
        ContactInfo()
    }
    
    return Tournament(
        id = id,
        name = name,
        description = description,
        startDate = startDate?.let { LocalDate.ofEpochDay(it) },
        endDate = endDate?.let { LocalDate.ofEpochDay(it) },
        venue = venue,
        registrationDeadline = registrationDeadline?.let { 
            LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC)
        },
        availableEvents = availableEvents,
        eventPrices = eventPrices,
        rules = rules,
        contactInfo = contactInfo,
        isActive = isActive,
        maxParticipants = maxParticipants,
        currentParticipants = currentParticipants,
        bannerImageUrl = bannerImageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Tournament.toEntity(): TournamentEntity {
    val gson = Gson()
    
    return TournamentEntity(
        id = id,
        name = name,
        description = description,
        startDate = startDate?.toEpochDay(),
        endDate = endDate?.toEpochDay(),
        venue = venue,
        registrationDeadline = registrationDeadline?.toEpochSecond(java.time.ZoneOffset.UTC),
        availableEventsJson = gson.toJson(availableEvents.map { it.name }),
        eventPricesJson = gson.toJson(eventPrices),
        rules = rules,
        contactInfoJson = gson.toJson(contactInfo),
        isActive = isActive,
        maxParticipants = maxParticipants,
        currentParticipants = currentParticipants,
        bannerImageUrl = bannerImageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}