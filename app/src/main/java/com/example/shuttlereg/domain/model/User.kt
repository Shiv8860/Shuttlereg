package com.example.shuttlereg.domain.model

import java.time.LocalDate

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: LocalDate? = null,
    val gender: Gender = Gender.MALE,
    val clubName: String? = null,
    val profilePhotoUrl: String? = null,
    val eligibleEvents: List<EventCategory> = emptyList(),
    val savedPartners: List<PartnerInfo> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)

enum class Gender {
    MALE, FEMALE
}

data class PartnerInfo(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: Gender = Gender.MALE,
    val isRegistered: Boolean = false
)