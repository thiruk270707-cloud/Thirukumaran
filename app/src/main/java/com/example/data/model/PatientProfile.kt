package com.example.data.model

data class PatientProfile(
    val name: String = "Sarah Jenkins",
    val dueDateMillis: Long = System.currentTimeMillis() + (58L * 24 * 60 * 60 * 1000), // ~8 weeks ahead
    val bloodGroup: String = "O+",
    val allergies: String = "Penicillin (Mild)",
    val doctorName: String = "Dr. Linda Vance, OB-GYN",
    val doctorPhone: String = "+1 (555) 234-5678",
    val familyPhone: String = "+1 (555) 876-5432",
    val backupPhone: String = "+1 (555) 345-6789"
)
