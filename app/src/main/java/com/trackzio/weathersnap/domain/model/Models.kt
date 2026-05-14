package com.trackzio.weathersnap.domain.model

data class CityResult(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val displayName: String = "$name, $country"
)

data class WeatherData(
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int
)