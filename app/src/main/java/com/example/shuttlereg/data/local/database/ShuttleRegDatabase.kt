package com.example.shuttlereg.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.shuttlereg.data.local.dao.TournamentDao
import com.example.shuttlereg.data.local.entity.TournamentEntity
import com.example.shuttlereg.data.local.converters.Converters

@Database(
    entities = [
        TournamentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShuttleRegDatabase : RoomDatabase() {
    
    abstract fun tournamentDao(): TournamentDao
    
    companion object {
        @Volatile
        private var INSTANCE: ShuttleRegDatabase? = null
        
        fun getDatabase(context: Context): ShuttleRegDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShuttleRegDatabase::class.java,
                    "shuttlereg_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}