package com.example.shuttlereg.data.remote.dto

import com.example.shuttlereg.domain.model.User
import com.example.shuttlereg.domain.model.Gender
import com.example.shuttlereg.domain.model.PartnerInfo
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId

data class UserDto(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: Timestamp? = null,
    val gender: String = "",
    val clubName: String? = null,
    val profilePhotoUrl: String? = null,
    val eligibleEvents: List<String> = emptyList(),
    val savedPartners: List<PartnerInfoDto> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)

data class PartnerInfoDto(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val isRegistered: Boolean = false
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        dateOfBirth = dateOfBirth?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate(),
        gender = Gender.valueOf(gender.uppercase()),
        clubName = clubName,
        profilePhotoUrl = profilePhotoUrl,
        eligibleEvents = eligibleEvents.mapNotNull { 
            try { 
                com.example.shuttlereg.domain.model.EventCategory.valueOf(it) 
            } catch (e: Exception) { 
                null 
            } 
        },
        savedPartners = savedPartners.map { it.toDomain() },
        createdAt = createdAt,
        isEmailVerified = isEmailVerified,
        isPhoneVerified = isPhoneVerified
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        dateOfBirth = dateOfBirth?.let { 
            Timestamp(java.util.Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        },
        gender = gender.name,
        clubName = clubName,
        profilePhotoUrl = profilePhotoUrl,
        eligibleEvents = eligibleEvents.map { it.name },
        savedPartners = savedPartners.map { it.toDto() },
        createdAt = createdAt,
        isEmailVerified = isEmailVerified,
        isPhoneVerified = isPhoneVerified
    )
}

fun PartnerInfoDto.toDomain(): PartnerInfo {
    return PartnerInfo(
        id = id,
        name = name,
        phone = phone,
        email = email,
        gender = Gender.valueOf(gender.uppercase()),
        isRegistered = isRegistered
    )
}

fun PartnerInfo.toDto(): PartnerInfoDto {
    return PartnerInfoDto(
        id = id,
        name = name,
        phone = phone,
        email = email,
        gender = gender.name,
        isRegistered = isRegistered
    )
}