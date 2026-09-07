package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contractions")
data class ContractionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationSeconds: Int,
    val intervalSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
