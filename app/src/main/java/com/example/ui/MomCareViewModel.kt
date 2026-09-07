package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.CurrentLocationInfo
import com.example.data.location.LocationHelper
import com.example.data.model.ContractionRecord
import com.example.data.model.PatientProfile
import com.example.data.model.SavedLocation
import com.example.data.model.SyncedContact
import com.example.data.repository.MomCareRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MomCareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MomCareRepository(application)

    // Current device location
    private val _currentLocation = MutableStateFlow(CurrentLocationInfo())
    val currentLocation: StateFlow<CurrentLocationInfo> = _currentLocation.asStateFlow()

    // Saved Locations from Room DB
    val savedLocations: StateFlow<List<SavedLocation>> = repository.allLocations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Synced Contacts from Contacts API
    private val _syncedContacts = MutableStateFlow<List<SyncedContact>>(emptyList())
    val syncedContacts: StateFlow<List<SyncedContact>> = _syncedContacts.asStateFlow()

    private val _isSyncingContacts = MutableStateFlow(false)
    val isSyncingContacts: StateFlow<Boolean> = _isSyncingContacts.asStateFlow()

    // Contractions history
    val contractions: StateFlow<List<ContractionRecord>> = repository.allContractions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Contraction stopwatch state
    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // Patient profile
    private val _patientProfile = MutableStateFlow(repository.getPatientProfile())
    val patientProfile: StateFlow<PatientProfile> = _patientProfile.asStateFlow()

    // Filter by category
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        // Refresh GPS position & seed initial medical emergency locations
        refreshLocation()
        // Load initial contacts in background
        syncContacts()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = LocationHelper.getCurrentLocation(getApplication())
            _currentLocation.value = loc
            repository.checkAndSeedInitialLocations(loc.latitude, loc.longitude)
        }
    }

    fun syncContacts() {
        viewModelScope.launch {
            _isSyncingContacts.value = true
            try {
                val current = _currentLocation.value
                val list = repository.syncContactsWithAddresses(current.latitude, current.longitude)
                _syncedContacts.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncingContacts.value = false
            }
        }
    }

    fun saveContactAsLocation(contact: SyncedContact) {
        viewModelScope.launch {
            val current = _currentLocation.value
            val lat = contact.latitude ?: (current.latitude + 0.005)
            val lng = contact.longitude ?: (current.longitude + 0.005)
            val savedLocation = com.example.data.contacts.ContactsHelper.toSavedLocation(
                contact.copy(latitude = lat, longitude = lng),
                lat,
                lng
            )
            repository.insertLocation(savedLocation)
            
            // Mark as saved in local list
            _syncedContacts.value = _syncedContacts.value.map {
                if (it.id == contact.id) it.copy(isSavedAsLocation = true) else it
            }
        }
    }

    fun addSavedLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.insertLocation(location)
        }
    }

    fun deleteSavedLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun startContractionTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    fun stopContractionTimer() {
        if (!_isTimerRunning.value) return
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null

        val seconds = _timerSeconds.value
        if (seconds > 0) {
            viewModelScope.launch {
                val lastRecord = contractions.value.firstOrNull()
                val interval = if (lastRecord != null) {
                    ((System.currentTimeMillis() - lastRecord.timestamp) / 1000).toInt()
                } else 0

                repository.insertContraction(
                    ContractionRecord(
                        durationSeconds = seconds,
                        intervalSeconds = interval,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun resetContractionTimer() {
        stopContractionTimer()
        _timerSeconds.value = 0
    }

    fun clearContractionHistory() {
        viewModelScope.launch {
            repository.clearContractions()
        }
    }

    fun updatePatientProfile(profile: PatientProfile) {
        _patientProfile.value = profile
        repository.savePatientProfile(profile)
    }
}
