package com.trackzio.weathersnap.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trackzio.weathersnap.domain.model.CityResult

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCityResultList(value: List<CityResult>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCityResultList(value: String?): List<CityResult>? {
        val listType = object : TypeToken<List<CityResult>>() {}.type
        return gson.fromJson(value, listType)
    }
}
