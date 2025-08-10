package com.example.shuttlereg.domain.repository

import com.example.shuttlereg.domain.model.Registration
import com.example.shuttlereg.domain.model.PaymentData
import kotlinx.coroutines.flow.Flow

interface RegistrationRepository {
    suspend fun createRegistration(registration: Registration): Result<String>
    suspend fun updateRegistration(registration: Registration): Result<Unit>
    suspend fun getRegistrationById(registrationId: String): Result<Registration>
    suspend fun getUserRegistrations(userId: String): Flow<List<Registration>>
    suspend fun getTournamentRegistrations(tournamentId: String): Flow<List<Registration>>
    suspend fun processPayment(registrationId: String, paymentData: PaymentData): Result<Unit>
    suspend fun generateRegistrationPDF(registrationId: String): Result<String>
    suspend fun cancelRegistration(registrationId: String): Result<Unit>
    suspend fun saveDraftRegistration(registration: Registration): Result<Unit>
    suspend fun getDraftRegistration(userId: String, tournamentId: String): Result<Registration?>
}