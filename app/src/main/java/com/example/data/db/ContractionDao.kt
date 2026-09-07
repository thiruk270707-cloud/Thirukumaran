package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ContractionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractionDao {
    @Query("SELECT * FROM contractions ORDER BY timestamp DESC")
    fun getAllContractions(): Flow<List<ContractionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContraction(record: ContractionRecord): Long

    @Query("DELETE FROM contractions")
    suspend fun clearAll()
}
