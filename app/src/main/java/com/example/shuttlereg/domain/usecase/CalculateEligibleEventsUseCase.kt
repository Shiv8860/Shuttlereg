package com.example.shuttlereg.domain.usecase

import com.example.shuttlereg.domain.model.EventCategory
import com.example.shuttlereg.domain.model.Gender
import java.time.LocalDate
import javax.inject.Inject

class CalculateEligibleEventsUseCase @Inject constructor() {
    
    operator fun invoke(dateOfBirth: LocalDate, gender: Gender): List<EventCategory> {
        val birthYear = dateOfBirth.year
        val eligibleEvents = mutableListOf<EventCategory>()
        
        // Age-based eligibility calculation
        if (birthYear >= 2016) eligibleEvents.add(EventCategory.U9)
        if (birthYear >= 2014) eligibleEvents.add(EventCategory.U11)
        if (birthYear >= 2012) eligibleEvents.add(EventCategory.U13)
        if (birthYear >= 2010) eligibleEvents.add(EventCategory.U15)
        if (birthYear >= 2008) eligibleEvents.add(EventCategory.U17)
        if (birthYear >= 2006) eligibleEvents.add(EventCategory.U19)
        
        // Open categories for 18+ (born 2006 or earlier)
        if (birthYear <= 2006) {
            when (gender) {
                Gender.MALE -> eligibleEvents.add(EventCategory.MENS_OPEN)
                Gender.FEMALE -> eligibleEvents.add(EventCategory.WOMENS_OPEN)
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
        val birthYear = dateOfBirth.year
        
        return when (category) {
            EventCategory.U9 -> birthYear >= 2016
            EventCategory.U11 -> birthYear >= 2014
            EventCategory.U13 -> birthYear >= 2012
            EventCategory.U15 -> birthYear >= 2010
            EventCategory.U17 -> birthYear >= 2008
            EventCategory.U19 -> birthYear >= 2006
            EventCategory.MENS_OPEN, EventCategory.WOMENS_OPEN -> birthYear <= 2006
        }
    }
}