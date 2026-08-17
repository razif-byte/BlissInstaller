package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installation_logs")
data class InstallationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceModel: String,
    val romName: String,
    val platformMode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val logs: String,
    val durationSeconds: Int,
    val isSyncedCloud: Boolean = false
)
