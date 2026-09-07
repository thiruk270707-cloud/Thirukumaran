package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.contacts.ContactsHelper
import com.example.data.db.MomCareDatabase
import com.example.data.location.LocationHelper
import com.example.data.model.ContractionRecord
import com.example.data.model.LocationCategory
import com.example.data.model.PatientProfile
import com.example.data.model.SavedLocation
import com.example.data.model.SyncedContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MomCareRepository(private val context: Context) {

    private val db = MomCareDatabase.getInstance(context)
    private val locationDao = db.locationDao()
    private val contractionDao = db.contractionDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("momcare_prefs", Context.MODE_PRIVATE)

    val allLocations: Flow<List<SavedLocation>> = locationDao.getAllLocations()
    val allContractions: Flow<List<ContractionRecord>> = contractionDao.getAllContractions()

    suspend fun checkAndSeedInitialLocations(baseLat: Double = 37.7749, baseLng: Double = -122.4194) = withContext(Dispatchers.IO) {
        val count = locationDao.getLocationCount()
        if (count == 0) {
            val initial = listOf(
                SavedLocation(
                    name = "St. Jude Women & Children's Emergency",
                    address = "1200 Mercy Parkway, Labor & Delivery ER",
                    latitude = baseLat - 0.008,
                    longitude = baseLng - 0.012,
                    category = LocationCategory.HOSPITAL.name,
                    phone = "102",
                    notes = "Primary 24/7 Level III NICU, trauma & obstetric center",
                    isContactSynced = false
                ),
                SavedLocation(
                    name = "City Care OB-GYN Center",
                    address = "450 Medical Center Blvd, Suite 300",
                    latitude = baseLat + 0.005,
                    longitude = baseLng + 0.008,
                    category = LocationCategory.CLINIC.name,
                    phone = "+1 (555) 234-5678",
                    notes = "Dr. Linda Vance clinic - ultrasound & emergency fetal monitoring",
                    isContactSynced = false
                ),
                SavedLocation(
                    name = "Central Urgent Maternal Care",
                    address = "890 Health Parkway, Sector 4",
                    latitude = baseLat + 0.012,
                    longitude = baseLng - 0.005,
                    category = LocationCategory.HOSPITAL.name,
                    phone = "112",
                    notes = "Rapid triage & ambulance bay entrance on south side",
                    isContactSynced = false
                )
            )
            locationDao.insertLocations(initial)
        }
    }

    suspend fun insertLocation(location: SavedLocation): Long = withContext(Dispatchers.IO) {
        locationDao.insertLocation(location)
    }

    suspend fun updateLocation(location: SavedLocation) = withContext(Dispatchers.IO) {
        locationDao.updateLocation(location)
    }

    suspend fun deleteLocation(location: SavedLocation) = withContext(Dispatchers.IO) {
        locationDao.deleteLocation(location)
    }

    suspend fun deleteLocationById(id: Long) = withContext(Dispatchers.IO) {
        locationDao.deleteLocationById(id)
    }

    /**
     * Reads device contacts with postal addresses and syncs them into SavedLocation
     */
    suspend fun syncContactsWithAddresses(baseLat: Double, baseLng: Double): List<SyncedContact> = withContext(Dispatchers.IO) {
        val contacts = ContactsHelper.fetchContactsWithAddresses(context)
        
        // Add them to database as SavedLocation if not already existing
        val locationsToInsert = mutableListOf<SavedLocation>()
        for ((index, contact) in contacts.withIndex()) {
            val offsetLat = contact.latitude ?: (baseLat + (index * 0.004) - 0.008)
            val offsetLng = contact.longitude ?: (baseLng + (index * 0.005) - 0.010)

            val location = ContactsHelper.toSavedLocation(
                contact = contact.copy(latitude = offsetLat, longitude = offsetLng),
                baseLat = offsetLat,
                baseLng = offsetLng
            )
            locationsToInsert.add(location)
        }

        if (locationsToInsert.isNotEmpty()) {
            locationDao.insertLocations(locationsToInsert)
        }

        contacts.map { contact ->
            contact.copy(isSavedAsLocation = true)
        }
    }

    suspend fun insertContraction(record: ContractionRecord) = withContext(Dispatchers.IO) {
        contractionDao.insertContraction(record)
    }

    suspend fun clearContractions() = withContext(Dispatchers.IO) {
        contractionDao.clearAll()
    }

    fun getPatientProfile(): PatientProfile {
        return PatientProfile(
            name = prefs.getString("name", "Sarah Jenkins") ?: "Sarah Jenkins",
            dueDateMillis = prefs.getLong("dueDateMillis", System.currentTimeMillis() + (58L * 24 * 60 * 60 * 1000)),
            bloodGroup = prefs.getString("bloodGroup", "O+") ?: "O+",
            allergies = prefs.getString("allergies", "Penicillin (Mild)") ?: "Penicillin (Mild)",
            doctorName = prefs.getString("doctorName", "Dr. Linda Vance, OB-GYN") ?: "Dr. Linda Vance, OB-GYN",
            doctorPhone = prefs.getString("doctorPhone", "+1 (555) 234-5678") ?: "+1 (555) 234-5678",
            familyPhone = prefs.getString("familyPhone", "+1 (555) 876-5432") ?: "+1 (555) 876-5432",
            backupPhone = prefs.getString("backupPhone", "+1 (555) 345-6789") ?: "+1 (555) 345-6789"
        )
    }

    fun savePatientProfile(profile: PatientProfile) {
        prefs.edit()
            .putString("name", profile.name)
            .putLong("dueDateMillis", profile.dueDateMillis)
            .putString("bloodGroup", profile.bloodGroup)
            .putString("allergies", profile.allergies)
            .putString("doctorName", profile.doctorName)
            .putString("doctorPhone", profile.doctorPhone)
            .putString("familyPhone", profile.familyPhone)
            .putString("backupPhone", profile.backupPhone)
            .apply()
    }
}
