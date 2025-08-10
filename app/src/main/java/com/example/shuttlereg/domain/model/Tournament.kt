package com.example.shuttlereg.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Tournament(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val venue: String = "",
    val registrationDeadline: LocalDateTime? = null,
    val availableEvents: List<EventCategory> = emptyList(),
    val eventPrices: Map<String, Double> = emptyMap(),
    val rules: String = "",
    val contactInfo: ContactInfo = ContactInfo(),
    val isActive: Boolean = true,
    val maxParticipants: Int = 0,
    val currentParticipants: Int = 0,
    val bannerImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ContactInfo(
    val organizerName: String = "",
    val email: String = "",
    val phone: String = "",
    val website: String? = null,
    val address: String = ""
)

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

data class EventSelection(
    val category: EventCategory,
    val type: EventType,
    val partnerInfo: PartnerInfo? = null,
    val price: Double = 0.0
)