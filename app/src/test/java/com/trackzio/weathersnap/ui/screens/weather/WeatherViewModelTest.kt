package com.trackzio.weathersnap.ui.screens.weather

import app.cash.turbine.test
import com.trackzio.weathersnap.domain.repository.WeatherRepository
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private lateinit var viewModel: WeatherViewModel
    private val repository: WeatherRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCityQueryChange triggers search after debounce`() = runTest {
        val query = "London"
        val mockResults = listOf(CityResult("London", "UK", 51.5, -0.12, "London, UK"))
        
        coEvery { repository.searchCities(query) } returns mockResults

        viewModel.citySuggestions.test {
            assertEquals(CitySuggestionState.Idle, awaitItem())
            
            viewModel.onCityQueryChange(query)
            
            // Advance time to pass debounce (300ms)
            advanceTimeBy(301)
            
            assertEquals(CitySuggestionState.Loading, awaitItem())
            val successState = awaitItem() as CitySuggestionState.Success
            assertEquals(mockResults, successState.cities)
        }
    }

    @Test
    fun `fetchWeather updates state to Success when repository returns data`() = runTest {
        val city = CityResult("London", "UK", 51.5, -0.12, "London, UK")
        val weatherData = WeatherData("London, UK", 20.0, "Clear", 50, 5.0, 1013)
        
        viewModel.onCitySelected(city)
        coEvery { repository.getWeather(city.latitude, city.longitude, city.displayName) } returns weatherData

        viewModel.weatherState.test {
            assertEquals(WeatherUiState.Idle, awaitItem())
            
            viewModel.fetchWeather()
            
            assertEquals(WeatherUiState.Loading, awaitItem())
            val successState = awaitItem() as WeatherUiState.Success
            assertEquals(weatherData, successState.data)
        }
    }
}
