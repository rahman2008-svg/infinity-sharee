package com.example.model

import java.io.Serializable

data class ShareableFile(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val type: String, // "PHOTO", "VIDEO", "MUSIC", "DOCUMENT", "APP"
    var isSelected: Boolean = false,
    val mimeType: String = "",
    val duration: String? = null,
    val dateModified: Long = System.currentTimeMillis()
) : Serializable {
    val sizeFormatted: String
        get() {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }
}
