package com.example.androidcoursework

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "audioRecords")
data class RecorderDataClass(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val filename: String,
    val filepath: String,
    val timestamp: Long,
    val duration: String,
    val latitude: Double?,
    val longitude: Double?

) {
    @Ignore
    var isChecked: Boolean = false
}

