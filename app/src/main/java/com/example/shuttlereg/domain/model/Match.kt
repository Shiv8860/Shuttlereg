package com.example.shuttlereg.domain.model

import java.time.LocalDateTime

data class Match(
    val id: String = "",
    val tournamentId: String = "",
    val tournamentName: String = "",
    val registrationId: String = "",
    val userId: String = "",
    val eventCategory: EventCategory,
    val eventType: EventType,
    val partnerName: String? = null,
    val partnerId: String? = null,
    val opponentName: String? = null,
    val opponentId: String? = null,
    val opponentPartnerName: String? = null,
    val scheduledDateTime: LocalDateTime? = null,
    val courtNumber: Int? = null,
    val round: String? = null, // e.g., "Quarterfinal", "Semifinal", "Final"
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val currentScore: String? = null, // e.g., "15-12" for live matches
    val finalScore: String? = null, // e.g., "21-18, 19-21, 21-15"
    val result: MatchResult? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MatchStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class MatchResult {
    WON,
    LOST,
    WALKOVER,
    DISQUALIFIED
}