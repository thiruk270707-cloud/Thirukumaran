package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.location.LocationHelper
import com.example.data.model.LocationCategory
import com.example.data.model.SavedLocation
import com.example.ui.MomCareViewModel
import com.example.ui.components.AddLocationDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.EmergencyDispatchDialog
import com.example.ui.components.InteractiveRadarMap
import com.example.ui.components.LaborTrackerSection
import com.example.ui.components.SavedLocationCard
import com.example.ui.components.SosBeaconSection
import com.example.ui.components.SyncedContactsSection
import com.example.ui.components.VitalsSection
import com.example.ui.theme.Cyan400
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Pink500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MainDashboardScreen(
    viewModel: MomCareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val syncedContacts by viewModel.syncedContacts.collectAsStateWithLifecycle()
    val isSyncingContacts by viewModel.isSyncingContacts.collectAsStateWithLifecycle()
    val contractions by viewModel.contractions.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val patientProfile by viewModel.patientProfile.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }

    // Request location and contacts permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val locGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val contactsGranted = results[Manifest.permission.READ_CONTACTS] == true

        if (locGranted) {
            viewModel.refreshLocation()
        }
        if (contactsGranted) {
            viewModel.syncContacts()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS
            )
        )
    }

    // Filtered locations
    val filteredLocations = remember(savedLocations, selectedCategory) {
        if (selectedCategory == null) {
            savedLocations
        } else {
            savedLocations.filter { it.category == selectedCategory }
        }
    }

    // Header heartbeat pulse
    val infiniteTransition = rememberInfiniteTransition(label = "header_heart")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            // Glass Top Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(listOf(Rose600, Pink500))
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(heartScale)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MomCare",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (currentLocation.isRealLock) Emerald400 else Rose400)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (currentLocation.isRealLock) "SATELLITE LINK ACTIVE" else "GPS STANDBY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (currentLocation.isRealLock) Emerald400 else Rose400,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                LocationHelper.searchNearbyHospitals(
                                    context,
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                )
                            },
                            modifier = Modifier.testTag("top_search_hospitals_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Find Nearby Hospitals",
                                tint = Rose400
                            )
                        }

                        IconButton(
                            onClick = { showAddLocationDialog = true },
                            modifier = Modifier.testTag("top_add_location_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = "Add Location",
                                tint = Cyan400
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.refreshLocation()
                                viewModel.syncContacts()
                                Toast.makeText(context, "Refreshing GPS & Contacts API...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("top_refresh_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Tabs
            val tabs = listOf("Overview & Radar", "Saved Locations", "Contacts API Sync", "Labor Tracker", "Vitals")
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = Rose400,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Rose500,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) Rose400 else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }

            // Main Content Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (selectedTab) {
                    0 -> {
                        // TAB 0: OVERVIEW & RADAR
                        item {
                            // Emergency SOS Central Beacon
                            SosBeaconSection(
                                onTriggerSOS = { showSosDialog = true }
                            )
                        }

                        item {
                            // Interactive GPS Radar Map
                            InteractiveRadarMap(
                                currentLocation = currentLocation,
                                savedLocations = savedLocations,
                                onOpenGoogleMaps = { loc ->
                                    LocationHelper.openGoogleMapsNavigation(
                                        context,
                                        loc.latitude,
                                        loc.longitude,
                                        loc.name
                                    )
                                },
                                onCallLocation = { phone ->
                                    LocationHelper.dialPhone(context, phone)
                                }
                            )
                        }

                        item {
                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        LocationHelper.searchNearbyHospitals(
                                            context,
                                            currentLocation.latitude,
                                            currentLocation.longitude
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("find_hospitals_google_maps_btn")
                                ) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("FIND HOSPITALS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        viewModel.syncContacts()
                                        selectedTab = 2 // Switch to Contacts API tab
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Cyan400.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .border(1.dp, Cyan400.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .testTag("quick_sync_contacts_btn")
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SYNC CONTACTS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Cyan400)
                                }
                            }
                        }

                        item {
                            // Patient Vitals Summary Card
                            VitalsSection(
                                profile = patientProfile,
                                onEditProfile = { showEditProfileDialog = true },
                                onShareLocation = {
                                    val link = "https://www.google.com/maps?q=${currentLocation.latitude},${currentLocation.longitude}"
                                    val text = "🚨 Emergency GPS Coordinates: $link\nPatient: ${patientProfile.name}\nAddress: ${currentLocation.address}"
                                    LocationHelper.shareViaWhatsApp(context, text)
                                },
                                onCall = { phone -> LocationHelper.dialPhone(context, phone) }
                            )
                        }
                    }

                    1 -> {
                        // TAB 1: SAVED LOCATIONS
                        item {
                            // Category Filter Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { viewModel.setCategoryFilter(null) },
                                    label = { Text("All (${savedLocations.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Rose500,
                                        selectedLabelColor = Color.White
                                    )
                                )

                                LocationCategory.values().forEach { cat ->
                                    val count = savedLocations.count { it.category == cat.name }
                                    FilterChip(
                                        selected = selectedCategory == cat.name,
                                        onClick = {
                                            viewModel.setCategoryFilter(if (selectedCategory == cat.name) null else cat.name)
                                        },
                                        label = { Text("${cat.displayName} ($count)") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Rose500,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        if (filteredLocations.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(DarkSurfaceCard)
                                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "No Locations Found",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Sync contacts with address data or tap 'Add Location' to save emergency medical centers.",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = { showAddLocationDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                                        ) {
                                            Text("Add Location Now", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredLocations, key = { it.id }) { loc ->
                                SavedLocationCard(
                                    location = loc,
                                    currentLat = currentLocation.latitude,
                                    currentLng = currentLocation.longitude,
                                    onNavigateWithMaps = { l ->
                                        LocationHelper.openGoogleMapsNavigation(
                                            context,
                                            l.latitude,
                                            l.longitude,
                                            l.name
                                        )
                                    },
                                    onCall = { phone -> LocationHelper.dialPhone(context, phone) },
                                    onDelete = { l -> viewModel.deleteSavedLocation(l) }
                                )
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: CONTACTS API SYNC
                        item {
                            SyncedContactsSection(
                                syncedContacts = syncedContacts,
                                isSyncing = isSyncingContacts,
                                onSyncContacts = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_CONTACTS,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                    viewModel.syncContacts()
                                },
                                onSaveContactAsLocation = { contact ->
                                    viewModel.saveContactAsLocation(contact)
                                    Toast.makeText(context, "${contact.name} saved to emergency locations", Toast.LENGTH_SHORT).show()
                                },
                                onNavigateWithMaps = { lat, lng, label ->
                                    LocationHelper.openGoogleMapsNavigation(context, lat, lng, label)
                                },
                                onCall = { phone -> LocationHelper.dialPhone(context, phone) }
                            )
                        }
                    }

                    3 -> {
                        // TAB 3: LABOR TRACKER
                        item {
                            LaborTrackerSection(
                                secondsElapsed = timerSeconds,
                                isRunning = isTimerRunning,
                                contractions = contractions,
                                onStartTimer = { viewModel.startContractionTimer() },
                                onStopTimer = { viewModel.stopContractionTimer() },
                                onResetTimer = { viewModel.resetContractionTimer() },
                                onClearHistory = { viewModel.clearContractionHistory() }
                            )
                        }
                    }

                    4 -> {
                        // TAB 4: VITALS & PROFILE
                        item {
                            VitalsSection(
                                profile = patientProfile,
                                onEditProfile = { showEditProfileDialog = true },
                                onShareLocation = {
                                    val link = "https://www.google.com/maps?q=${currentLocation.latitude},${currentLocation.longitude}"
                                    val text = "🚨 Emergency GPS Coordinates: $link\nPatient: ${patientProfile.name}\nAddress: ${currentLocation.address}"
                                    LocationHelper.shareViaWhatsApp(context, text)
                                },
                                onCall = { phone -> LocationHelper.dialPhone(context, phone) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // SOS Emergency Dispatch Confirmation Dialog
    if (showSosDialog) {
        EmergencyDispatchDialog(
            profile = patientProfile,
            currentLocation = currentLocation,
            onDismiss = { showSosDialog = false },
            onSendSMS = { phone, payload ->
                LocationHelper.sendSMS(context, phone, payload)
                showSosDialog = false
            },
            onShareWhatsApp = { payload ->
                LocationHelper.shareViaWhatsApp(context, payload)
                showSosDialog = false
            },
            onCallEmergency = { num ->
                LocationHelper.dialPhone(context, num)
                showSosDialog = false
            },
            onOpenRouteMap = {
                LocationHelper.searchNearbyHospitals(
                    context,
                    currentLocation.latitude,
                    currentLocation.longitude
                )
                showSosDialog = false
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = patientProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updatePatientProfile(updated)
                showEditProfileDialog = false
                Toast.makeText(context, "Medical Profile Updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Location Dialog
    if (showAddLocationDialog) {
        AddLocationDialog(
            currentLat = currentLocation.latitude,
            currentLng = currentLocation.longitude,
            onDismiss = { showAddLocationDialog = false },
            onSave = { newLoc ->
                viewModel.addSavedLocation(newLoc)
                showAddLocationDialog = false
                Toast.makeText(context, "Location Saved: ${newLoc.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
