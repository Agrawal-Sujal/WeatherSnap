package com.trackzio.weathersnap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trackzio.weathersnap.data.local.entity.WeatherReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherReportDao {
    @Query("SELECT * FROM weather_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WeatherReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WeatherReportEntity): Long

    @Query("DELETE FROM weather_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}
