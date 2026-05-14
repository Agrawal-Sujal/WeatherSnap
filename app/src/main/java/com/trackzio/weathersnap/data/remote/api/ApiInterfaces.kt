package com.trackzio.weathersnap.data.remote.api
import com.trackzio.weathersnap.data.remote.model.GeocodingResponse
import com.trackzio.weathersnap.data.remote.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,weather_code,is_day",
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
        @Query("forecast_days") forecastDays: Int = 1
    ): WeatherResponse
}