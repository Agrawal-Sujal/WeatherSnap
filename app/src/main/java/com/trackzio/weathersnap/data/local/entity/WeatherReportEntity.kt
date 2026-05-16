package com.trackzio.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_reports")
data class WeatherReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    val imagePath: String,
    val originalSizeKb: Long,
    val compressedSizeKb: Long,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)
