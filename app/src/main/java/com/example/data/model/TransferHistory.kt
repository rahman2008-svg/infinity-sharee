package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transfer_history")
data class TransferHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val fileType: String, // "PHOTO", "VIDEO", "MUSIC", "DOCUMENT", "APP"
    val direction: String, // "SEND", "RECEIVE"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "COMPLETED", "FAILED", "IN_PROGRESS"
    val transferSpeed: String = "0 KB/s"
) : Serializable
