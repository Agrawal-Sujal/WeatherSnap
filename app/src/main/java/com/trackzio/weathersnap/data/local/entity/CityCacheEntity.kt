package com.trackzio.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trackzio.weathersnap.domain.model.CityResult

@Entity(tableName = "city_cache")
data class CityCacheEntity(
    @PrimaryKey val query: String,
    val cities: List<CityResult>,
    val timestamp: Long = System.currentTimeMillis()
)
