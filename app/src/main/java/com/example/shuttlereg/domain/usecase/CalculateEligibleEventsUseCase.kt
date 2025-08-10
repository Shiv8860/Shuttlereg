package com.example.shuttlereg.domain.usecase

import com.example.shuttlereg.domain.model.EventCategory
import com.example.shuttlereg.domain.model.Gender
import java.time.LocalDate
import javax.inject.Inject

class CalculateEligibleEventsUseCase @Inject constructor() {
    
    operator fun invoke(dateOfBirth: LocalDate, gender: Gender): List<EventCategory> {
        val eligibleEvents = mutableListOf<EventCategory>()
        
        // Add all eligible categories based on individual eligibility
        if (isEligibleForCategory(dateOfBirth, EventCategory.U9)) eligibleEvents.add(EventCategory.U9)
        if (isEligibleForCategory(dateOfBirth, EventCategory.U11)) eligibleEvents.add(EventCategory.U11)
        if (isEligibleForCategory(dateOfBirth, EventCategory.U13)) eligibleEvents.add(EventCategory.U13)
        if (isEligibleForCategory(dateOfBirth, EventCategory.U15)) eligibleEvents.add(EventCategory.U15)
        if (isEligibleForCategory(dateOfBirth, EventCategory.U17)) eligibleEvents.add(EventCategory.U17)
        if (isEligibleForCategory(dateOfBirth, EventCategory.U19)) eligibleEvents.add(EventCategory.U19)
        
        // Add gender-specific open categories if eligible
        when (gender) {
            Gender.MALE -> {
                if (isEligibleForCategory(dateOfBirth, EventCategory.MENS_OPEN)) {
                    eligibleEvents.add(EventCategory.MENS_OPEN)
                }
            }
            Gender.FEMALE -> {
                if (isEligibleForCategory(dateOfBirth, EventCategory.WOMENS_OPEN)) {
                    eligibleEvents.add(EventCategory.WOMENS_OPEN)
                }
            }
        }
        
        return eligibleEvents
    }
    
    fun getAgeFromDateOfBirth(dateOfBirth: LocalDate): Int {
        val currentDate = LocalDate.now()
        return currentDate.year - dateOfBirth.year - 
            if (currentDate.dayOfYear < dateOfBirth.dayOfYear) 1 else 0
    }
    
    fun isEligibleForCategory(dateOfBirth: LocalDate, category: EventCategory): Boolean {
        val currentYear = LocalDate.now().year
        val age = currentYear - dateOfBirth.year
        
        return when (category) {
            EventCategory.U9 -> age <= 8 // Up to 8 years old
            EventCategory.U11 -> age <= 10 // Up to 10 years old  
            EventCategory.U13 -> age <= 12 // Up to 12 years old
            EventCategory.U15 -> age <= 14 // Up to 14 years old
            EventCategory.U17 -> age <= 16 // Up to 16 years old
            EventCategory.U19 -> age <= 18 // Up to 18 years old
            EventCategory.MENS_OPEN, EventCategory.WOMENS_OPEN -> age >= 18 // 18 and above
        }
    }
}