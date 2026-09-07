package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ContractionRecord
import com.example.data.model.SavedLocation

@Database(
    entities = [SavedLocation::class, ContractionRecord::class],
    version = 1,
    exportSchema = false
)
abstract class MomCareDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun contractionDao(): ContractionDao

    companion object {
        @Volatile
        private var INSTANCE: MomCareDatabase? = null

        fun getInstance(context: Context): MomCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MomCareDatabase::class.java,
                    "momcare_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
