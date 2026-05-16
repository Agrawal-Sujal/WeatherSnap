package com.trackzio.weathersnap.data

import com.trackzio.weathersnap.data.local.dao.CityCacheDao
import com.trackzio.weathersnap.data.local.dao.WeatherReportDao
import com.trackzio.weathersnap.data.remote.api.GeocodingApi
import com.trackzio.weathersnap.data.remote.api.WeatherApi
import com.trackzio.weathersnap.data.remote.model.GeocodingResponse
import com.trackzio.weathersnap.data.remote.model.GeocodingResult
import com.trackzio.weathersnap.data.local.entity.CityCacheEntity
import com.trackzio.weathersnap.data.repository.WeatherRepositoryImpl
import com.trackzio.weathersnap.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var repository: WeatherRepository
    private val geocodingApi: GeocodingApi = mockk()
    private val weatherApi: WeatherApi = mockk()
    private val reportDao: WeatherReportDao = mockk()
    private val cityCacheDao: CityCacheDao = mockk()

    @Before
    fun setup() {
        repository = WeatherRepositoryImpl(geocodingApi, weatherApi, reportDao, cityCacheDao)
    }

    @Test
    fun `searchCities returns cached results when valid cache exists`() = runTest {
        val query = "London"
        val cacheKey = "london"
        val cachedCities = listOf(
            com.trackzio.weathersnap.domain.model.CityResult("London", "UK", 51.5, -0.12, "London, UK")
        )
        val cacheEntity = CityCacheEntity(cacheKey, cachedCities, System.currentTimeMillis())

        coEvery { cityCacheDao.getCache(cacheKey) } returns cacheEntity

        val result = repository.searchCities(query)

        assertEquals(cachedCities, result)
        coVerify(exactly = 0) { geocodingApi.searchCities(any()) }
    }

    @Test
    fun `searchCities fetches from API and saves to cache when no cache exists`() = runTest {
        val query = "London"
        val cacheKey = "london"
        
        coEvery { cityCacheDao.getCache(cacheKey) } returns null
        
        val apiResults = listOf(
            GeocodingResult("London", "UK", 51.5, -0.12, "Greater London")
        )
        coEvery { geocodingApi.searchCities(query) } returns GeocodingResponse(apiResults)
        coEvery { cityCacheDao.insertCache(any()) } returns Unit

        val result = repository.searchCities(query)

        assertEquals(1, result.size)
        assertEquals("London", result[0].name)
        assertEquals("London, Greater London, UK", result[0].displayName)
        
        coVerify { geocodingApi.searchCities(query) }
        coVerify { cityCacheDao.insertCache(match { it.query == cacheKey && it.cities.size == 1 }) }
    }
}
