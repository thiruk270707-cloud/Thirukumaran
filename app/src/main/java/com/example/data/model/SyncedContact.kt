package com.example.data.model

data class SyncedContact(
    val id: String,
    val name: String,
    val phone: String,
    val formattedAddress: String,
    val street: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSavedAsLocation: Boolean = false
)
