package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

data class CurrentLocationInfo(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val address: String = "Locating GPS...",
    val isRealLock: Boolean = false,
    val accuracyMeters: Float = 0f
)

object LocationHelper {

    /**
     * Attempts to acquire high-accuracy current location via FusedLocationProviderClient
     * with fallback to LocationManager or sensible defaults.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): CurrentLocationInfo = withContext(Dispatchers.IO) {
        var lat = 37.7749
        var lng = -122.4194
        var isLocked = false
        var accuracy = 10f

        try {
            val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            val location: Location? = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).await()

            if (location != null) {
                lat = location.latitude
                lng = location.longitude
                accuracy = location.accuracy
                isLocked = true
            } else {
                val lastLoc = fusedClient.lastLocation.await()
                if (lastLoc != null) {
                    lat = lastLoc.latitude
                    lng = lastLoc.longitude
                    accuracy = lastLoc.accuracy
                    isLocked = true
                } else {
                    // Fallback to LocationManager
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val gpsLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val netLoc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val bestLoc = gpsLoc ?: netLoc
                    if (bestLoc != null) {
                        lat = bestLoc.latitude
                        lng = bestLoc.longitude
                        accuracy = bestLoc.accuracy
                        isLocked = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val resolvedAddress = reverseGeocode(context, lat, lng)
        CurrentLocationInfo(
            latitude = lat,
            longitude = lng,
            address = resolvedAddress,
            isRealLock = isLocked,
            accuracyMeters = accuracy
        )
    }

    /**
     * Resolves human-readable street and city from coordinates
     */
    fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!results.isNullOrEmpty()) {
                val address = results[0]
                val street = address.thoroughfare ?: address.featureName ?: ""
                val subThoroughfare = address.subThoroughfare ?: ""
                val locality = address.locality ?: address.subAdminArea ?: ""
                val admin = address.adminArea ?: ""
                
                val fullStreet = if (subThoroughfare.isNotEmpty()) "$subThoroughfare $street".trim() else street
                listOfNotNull(
                    fullStreet.takeIf { it.isNotEmpty() },
                    locality.takeIf { it.isNotEmpty() },
                    admin.takeIf { it.isNotEmpty() }
                ).joinToString(", ")
            } else {
                "GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
            }
        } catch (e: Exception) {
            "GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
        }
    }

    /**
     * Distance in kilometers between two coordinates
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000.0
    }

    fun formatDistance(km: Double): String {
        return if (km < 1.0) {
            "${(km * 1000).roundToInt()} m"
        } else {
            "${"%.1f".format(km)} km"
        }
    }

    /**
     * Launches Google Maps app or browser navigation to destination
     */
    fun openGoogleMapsNavigation(context: Context, destLat: Double, destLng: Double, label: String) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$destLat,$destLng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to web browser or generic geo intent
            val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Opens Google Maps searching for nearby hospitals / emergency rooms around the current location
     */
    fun searchNearbyHospitals(context: Context, currentLat: Double, currentLng: Double) {
        val query = "emergency hospital maternity near me"
        val webUri = Uri.parse("https://www.google.com/maps/search/${Uri.encode(query)}/@$currentLat,$currentLng,14z")
        val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Direct Phone Dialer
     */
    fun dialPhone(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Direct SMS intent with pre-filled SOS payload
     */
    fun sendSMS(context: Context, phoneNumber: String, messageText: String) {
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val uri = Uri.parse("smsto:$cleanNumber")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback generic send
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$cleanNumber?body=${Uri.encode(messageText)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Share SOS emergency payload to WhatsApp
     */
    fun shareViaWhatsApp(context: Context, messageText: String) {
        val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(messageText)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // General share sheet fallback
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Dispatch Emergency Alert"))
        }
    }
}
