package com.trackzio.weathersnap.data.local
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trackzio.weathersnap.domain.model.CityResult
import kotlinx.coroutines.flow.Flow

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

@Dao
interface WeatherReportDao {
    @Query("SELECT * FROM weather_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<WeatherReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WeatherReportEntity): Long

    @Query("DELETE FROM weather_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}

@Entity(tableName = "city_cache")
data class CityCacheEntity(
    @PrimaryKey val query: String,
    val cities: List<CityResult>,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CityCacheDao {
    @Query("SELECT * FROM city_cache WHERE `query` = :query")
    suspend fun getCache(query: String): CityCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CityCacheEntity)

    @Query("DELETE FROM city_cache WHERE timestamp < :expiryTime")
    suspend fun clearOldCache(expiryTime: Long)
}

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

@Database(
    entities = [WeatherReportEntity::class, CityCacheEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherSnapDatabase : RoomDatabase() {
    abstract fun weatherReportDao(): WeatherReportDao
    abstract fun cityCacheDao(): CityCacheDao
}