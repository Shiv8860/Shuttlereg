package com.example.shuttlereg.di

import com.example.shuttlereg.data.repository.AuthRepositoryImpl
import com.example.shuttlereg.data.repository.PhotoRepositoryImpl
import com.example.shuttlereg.data.repository.UserRepositoryImpl
import com.example.shuttlereg.data.repository.TournamentRepositoryImpl
import com.example.shuttlereg.domain.repository.AuthRepository
import com.example.shuttlereg.domain.repository.PhotoRepository
import com.example.shuttlereg.domain.repository.UserRepository
import com.example.shuttlereg.domain.repository.TournamentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindTournamentRepository(
        tournamentRepositoryImpl: TournamentRepositoryImpl
    ): TournamentRepository
    
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository
    
    // TODO: Add RegistrationRepository binding when implemented
    // @Binds
    // @Singleton
    // abstract fun bindRegistrationRepository(
    //     registrationRepositoryImpl: RegistrationRepositoryImpl
    // ): RegistrationRepository
}