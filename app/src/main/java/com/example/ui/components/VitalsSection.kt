package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientProfile
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Pink500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VitalsSection(
    profile: PatientProfile,
    onEditProfile: () -> Unit,
    onShareLocation: () -> Unit,
    onCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dueDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDateStr = dueDateFormat.format(Date(profile.dueDateMillis))

    // Estimate gestation: standard pregnancy = 280 days
    val now = System.currentTimeMillis()
    val conceptionTime = profile.dueDateMillis - (280L * 24 * 60 * 60 * 1000)
    val elapsedDays = ((now - conceptionTime) / (24L * 60 * 60 * 1000)).coerceIn(0, 280)
    val weeks = elapsedDays / 7
    val days = elapsedDays % 7
    val gestationStr = "$weeks Weeks + $days Days"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Rose500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = Rose400,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Patient Vitals & Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = profile.name,
                        fontSize = 11.sp,
                        color = Rose400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedButton(
                onClick = onEditProfile,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("edit_profile_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vitals Grid (2x3)
        val vitals = listOf(
            "EXPECTED DUE DATE" to dueDateStr,
            "GESTATIONAL AGE" to gestationStr,
            "BLOOD GROUP" to profile.bloodGroup,
            "ALLERGIES" to profile.allergies,
            "PRIMARY DOCTOR" to profile.doctorName,
            "DOCTOR PHONE" to profile.doctorPhone
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in vitals.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Item 1
                    VitalCard(
                        title = vitals[i].first,
                        value = vitals[i].second,
                        isHighlight = i == 1,
                        modifier = Modifier.weight(1f)
                    )
                    // Item 2
                    if (i + 1 < vitals.size) {
                        VitalCard(
                            title = vitals[i + 1].first,
                            value = vitals[i + 1].second,
                            isHighlight = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Priority Speed Dial Buttons
        Text(
            text = "PRIORITY SPEED DIAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedDialButton(
                emoji = "👨",
                label = "Family",
                phone = profile.familyPhone,
                onClick = { onCall(profile.familyPhone) },
                modifier = Modifier.weight(1f)
            )
            SpeedDialButton(
                emoji = "👩‍⚕️",
                label = "Doctor",
                phone = profile.doctorPhone,
                onClick = { onCall(profile.doctorPhone) },
                modifier = Modifier.weight(1f)
            )
            SpeedDialButton(
                emoji = "🚑",
                label = "102 / 112",
                phone = "102",
                onClick = { onCall("102") },
                modifier = Modifier.weight(1f)
            )
            SpeedDialButton(
                emoji = "👨‍👩‍👧",
                label = "Backup",
                phone = profile.backupPhone,
                onClick = { onCall(profile.backupPhone) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Share Location Button
        Button(
            onClick = onShareLocation,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("share_location_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ShareLocation,
                contentDescription = null,
                tint = Rose400,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Emergency GPS Link", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VitalCard(
    title: String,
    value: String,
    isHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF030712))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Rose400 else TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpeedDialButton(
    emoji: String,
    label: String,
    phone: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF030712))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
