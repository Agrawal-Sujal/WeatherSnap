package com.trackzio.weathersnap.domain.repository

import com.trackzio.weathersnap.data.local.entity.WeatherReportEntity
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun searchCities(query: String): List<CityResult>
    suspend fun getWeather(latitude: Double, longitude: Double, cityName: String): WeatherData
    fun getAllReports(): Flow<List<WeatherReportEntity>>
    suspend fun saveReport(report: WeatherReportEntity): Long
}

class NetworkUnavailableException : Exception("No internet connection")
