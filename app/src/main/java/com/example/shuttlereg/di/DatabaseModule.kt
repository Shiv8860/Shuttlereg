package com.example.shuttlereg.di

import android.content.Context
import androidx.room.Room
import com.example.shuttlereg.data.local.database.ShuttleRegDatabase
import com.example.shuttlereg.data.local.dao.TournamentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideShuttleRegDatabase(@ApplicationContext context: Context): ShuttleRegDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ShuttleRegDatabase::class.java,
            "shuttlereg_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTournamentDao(database: ShuttleRegDatabase): TournamentDao {
        return database.tournamentDao()
    }
}