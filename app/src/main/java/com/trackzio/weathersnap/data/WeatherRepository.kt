package com.trackzio.weathersnap.data

import com.trackzio.weathersnap.data.local.CityCacheDao
import com.trackzio.weathersnap.data.local.CityCacheEntity
import com.trackzio.weathersnap.data.local.WeatherReportDao
import com.trackzio.weathersnap.data.local.WeatherReportEntity
import com.trackzio.weathersnap.data.remote.api.GeocodingApi
import com.trackzio.weathersnap.data.remote.api.WeatherApi
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val reportDao: WeatherReportDao,
    private val cityCacheDao: CityCacheDao
) {
    suspend fun searchCities(query: String): List<CityResult> {
        val cacheKey = query.lowercase().trim()

        // 1. Check persistent cache
        cityCacheDao.getCache(cacheKey)?.let {
            if (System.currentTimeMillis() - it.timestamp < 24 * 60 * 60 * 1000) {
                return it.cities
            }
        }

        // 2. Fetch from API
        val response = try {
            geocodingApi.searchCities(query)
        } catch (e: Exception) {
            null
        }

        val results = response?.results?.map { result ->
            CityResult(
                name = result.name,
                country = result.country,
                latitude = result.latitude,
                longitude = result.longitude,
                displayName = buildString {
                    append(result.name)
                    result.admin1?.let { append(", $it") }
                    append(", ${result.country}")
                }
            )
        } ?: emptyList()

        // 3. Save to persistent cache
        if (results.isNotEmpty()) {
            cityCacheDao.insertCache(CityCacheEntity(cacheKey, results))
        }

        return results
    }

    /**
     * Fetches weather from the network.
     * Throws [NetworkUnavailableException] when the device is offline so the
     * ViewModel can show the offline-fallback state instead of a generic error.
     */
    suspend fun getWeather(latitude: Double, longitude: Double, cityName: String): WeatherData {
        try {
            val response = weatherApi.getWeather(latitude, longitude)
            val current = response.current
            return WeatherData(
                cityName = cityName,
                temperature = current.temperature,
                condition = weatherCodeToCondition(current.weatherCode),
                humidity = current.humidity,
                windSpeed = current.windSpeed,
                pressure = current.pressure.toInt()
            )
        } catch (e: IOException) {
            // No connectivity or socket timeout — signal offline state
            throw NetworkUnavailableException()
        }
    }

    fun getAllReports(): Flow<List<WeatherReportEntity>> = reportDao.getAllReports()

    suspend fun saveReport(report: WeatherReportEntity): Long =
        reportDao.insertReport(report)

    private fun weatherCodeToCondition(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }
}

class NetworkUnavailableException : Exception("No internet connection")