package com.trackzio.weathersnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trackzio.weathersnap.data.local.dao.CityCacheDao
import com.trackzio.weathersnap.data.local.dao.WeatherReportDao
import com.trackzio.weathersnap.data.local.entity.CityCacheEntity
import com.trackzio.weathersnap.data.local.entity.WeatherReportEntity

@Database(
    entities = [
        WeatherReportEntity::class,
        CityCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherSnapDatabase : RoomDatabase() {
    abstract fun weatherReportDao(): WeatherReportDao
    abstract fun cityCacheDao(): CityCacheDao
}
