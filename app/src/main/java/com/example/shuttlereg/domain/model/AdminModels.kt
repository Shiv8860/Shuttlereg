package com.example.shuttlereg.domain.model

data class AdminStats(
    val totalPlayers: Int = 0,
    val totalMatches: Int = 0,
    val totalCourts: Int = 4,
    val totalRevenue: Double = 0.0,
    val pendingPayments: Int = 0,
    val completedMatches: Int = 0,
    val ongoingMatches: Int = 0
)

data class AdminNotification(
    val id: String = "",
    val type: AdminNotificationType,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AdminNotificationType {
    PAYMENT_PENDING,
    PLAYER_REQUEST,
    SYSTEM_ALERT
}