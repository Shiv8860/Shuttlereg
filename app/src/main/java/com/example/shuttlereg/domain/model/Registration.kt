package com.example.shuttlereg.domain.model

import java.time.LocalDateTime

data class Registration(
    val id: String = "",
    val userId: String = "",
    val tournamentId: String = "",
    val selectedEvents: List<EventSelection> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentId: String? = null,
    val registrationDate: LocalDateTime? = null,
    val pdfUrl: String? = null,
    val status: RegistrationStatus = RegistrationStatus.DRAFT,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED
}

enum class RegistrationStatus {
    DRAFT,
    SUBMITTED,
    CONFIRMED,
    CANCELLED,
    WAITLISTED
}

data class PaymentData(
    val paymentId: String,
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
    val method: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class EventRegistration(
    val id: String = "",
    val registrationId: String = "",
    val eventType: EventType,
    val category: EventCategory,
    val partnerInfo: PartnerInfo? = null,
    val confirmed: Boolean = false,
    val drawPosition: Int? = null,
    val seedNumber: Int? = null
)