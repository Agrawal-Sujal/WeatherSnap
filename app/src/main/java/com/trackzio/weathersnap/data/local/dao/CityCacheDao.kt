package com.trackzio.weathersnap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trackzio.weathersnap.data.local.entity.CityCacheEntity

@Dao
interface CityCacheDao {
    @Query("SELECT * FROM city_cache WHERE `query` = :query")
    suspend fun getCache(query: String): CityCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CityCacheEntity)

    @Query("DELETE FROM city_cache WHERE timestamp < :expiryTime")
    suspend fun clearOldCache(expiryTime: Long)
}
