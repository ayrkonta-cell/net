package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_history")
data class NetworkHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val testType: String, // "SPEED" or "PING"
    val downloadSpeedMbps: Double,
    val pingMs: Double,
    val jitterMs: Double,
    val packetLossPercent: Int,
    val connectionType: String, // "Wi-Fi", "Cellular", "Offline"
    val ipAddress: String,
    val isSuccess: Boolean
)
