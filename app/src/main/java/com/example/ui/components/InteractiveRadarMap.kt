package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.CurrentLocationInfo
import com.example.data.location.LocationHelper
import com.example.data.model.LocationCategory
import com.example.data.model.SavedLocation
import com.example.ui.theme.Cyan400
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Pink500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveRadarMap(
    currentLocation: CurrentLocationInfo,
    savedLocations: List<SavedLocation>,
    onOpenGoogleMaps: (SavedLocation) -> Unit,
    onCallLocation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLocation by remember { mutableStateOf<SavedLocation?>(null) }

    // Radar sweep rotation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(28.dp))
            .padding(16.dp)
    ) {
        // Radar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (currentLocation.isRealLock) Emerald400 else Rose500)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLocation.isRealLock) "GPS RADAR ACTIVE" else "ESTIMATED POSITION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentLocation.isRealLock) Emerald400 else Rose400,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "${savedLocations.size} Points Plotted",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Radar Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF030712))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
        ) {
            // Screen coordinates calculated for tapping
            var markerPositions by remember { mutableStateOf<List<Pair<Offset, SavedLocation>>>(emptyList()) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(savedLocations) {
                        detectTapGestures { tapOffset ->
                            // Find clicked marker within 28dp radius
                            val clicked = markerPositions.find { (pos, _) ->
                                val dx = tapOffset.x - pos.x
                                val dy = tapOffset.y - pos.y
                                (dx * dx + dy * dy) <= 1200f
                            }?.second

                            selectedLocation = clicked
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (minOf(size.width, size.height) / 2f) - 24f

                // Draw Grid crosshairs
                drawLine(
                    color = Color(0x2238BDF8),
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0x2238BDF8),
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1f
                )

                // Concentric radar rings
                val rings = 3
                for (i in 1..rings) {
                    val r = maxRadius * (i / rings.toFloat())
                    drawCircle(
                        color = Color(0x2538BDF8),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.2f)
                    )
                }

                // Rotating radar sweep line
                val rad = (sweepAngle * (PI / 180f)).toFloat()
                val sweepTarget = Offset(
                    center.x + maxRadius * cos(rad),
                    center.y + maxRadius * sin(rad)
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x88F43F5E), Color.Transparent),
                        start = center,
                        end = sweepTarget
                    ),
                    start = center,
                    end = sweepTarget,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Current Location pulse
                drawCircle(
                    color = Emerald400.copy(alpha = 0.25f),
                    radius = 16f * pulseScale,
                    center = center
                )
                drawCircle(
                    color = Emerald400,
                    radius = 6f,
                    center = center
                )

                // Plot saved locations relative to current coordinates
                val positions = mutableListOf<Pair<Offset, SavedLocation>>()
                val scaleFactor = 4000f // coordinate span scaling

                savedLocations.forEachIndexed { index, loc ->
                    val dLat = loc.latitude - currentLocation.latitude
                    val dLng = loc.longitude - currentLocation.longitude

                    // Clamp to radar radius
                    var px = center.x + (dLng * scaleFactor).toFloat()
                    var py = center.y - (dLat * scaleFactor).toFloat()

                    val distFromCenter = kotlin.math.sqrt((px - center.x) * (px - center.x) + (py - center.y) * (py - center.y))
                    if (distFromCenter > maxRadius - 15f) {
                        val angle = kotlin.math.atan2(py - center.y, px - center.x)
                        px = center.x + (maxRadius - 20f) * cos(angle)
                        py = center.y + (maxRadius - 20f) * sin(angle)
                    }

                    val markerOffset = Offset(px, py)
                    positions.add(markerOffset to loc)

                    val isSelected = selectedLocation?.id == loc.id
                    val markerColor = when (loc.category) {
                        LocationCategory.HOSPITAL.name -> Rose500
                        LocationCategory.CLINIC.name -> Pink500
                        LocationCategory.CONTACT_ADDRESS.name -> Cyan400
                        LocationCategory.HOME.name -> Emerald400
                        else -> Color(0xFFFBBF24)
                    }

                    if (isSelected) {
                        drawCircle(
                            color = markerColor.copy(alpha = 0.4f),
                            radius = 18f,
                            center = markerOffset
                        )
                    }

                    drawCircle(
                        color = markerColor,
                        radius = if (isSelected) 10f else 7f,
                        center = markerOffset
                    )

                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 4f else 2.5f,
                        center = markerOffset
                    )
                }

                markerPositions = positions
            }

            // Radar compass indicators
            Text(
                text = "N",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
            )
            Text(
                text = "5 km",
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }

        // Selected Location Quick Callout Banner
        selectedLocation?.let { loc ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (loc.isContactSynced) Icons.Default.PersonPinCircle else Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Rose400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = loc.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        IconButton(
                            onClick = { selectedLocation = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = loc.address,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    val dist = LocationHelper.calculateDistanceKm(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        loc.latitude,
                        loc.longitude
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distance: ${LocationHelper.formatDistance(dist)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald400
                        )

                        Row {
                            if (loc.phone.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { onCallLocation(loc.phone) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("radar_call_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = { onOpenGoogleMaps(loc) },
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("radar_maps_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
