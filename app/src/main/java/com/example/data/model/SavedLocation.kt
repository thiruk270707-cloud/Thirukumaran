package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LocationCategory(val displayName: String) {
    HOSPITAL("Hospital / Emergency"),
    CLINIC("OB-GYN / Clinic"),
    CONTACT_ADDRESS("Contact Address"),
    HOME("Home"),
    PHARMACY("Pharmacy"),
    OTHER("Custom Point")
}

@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: String = LocationCategory.HOSPITAL.name,
    val phone: String = "",
    val notes: String = "",
    val isContactSynced: Boolean = false,
    val contactName: String = "",
    val contactId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
