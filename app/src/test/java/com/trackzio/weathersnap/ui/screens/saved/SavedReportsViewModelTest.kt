package com.trackzio.weathersnap.ui.screens.saved

import app.cash.turbine.test
import com.trackzio.weathersnap.data.WeatherRepository
import com.trackzio.weathersnap.data.local.WeatherReportEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedReportsViewModelTest {

    private lateinit var viewModel: SavedReportsViewModel
    private val repository: WeatherRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock the flow before ViewModel initialization if it calls it immediately
        // In this ViewModel, it's a flatMapLatest on a trigger
        every { repository.getAllReports() } returns flowOf(emptyList())
        
        viewModel = SavedReportsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits Success when repository returns reports`() = runTest {
        val mockReports = listOf(
            WeatherReportEntity(
                id = 1,
                cityName = "London",
                temperature = 20.0,
                condition = "Clear",
                humidity = 50,
                windSpeed = 5.0,
                pressure = 1013,
                imagePath = "path/to/image",
                originalSizeKb = 1000,
                compressedSizeKb = 100,
                notes = "Nice weather"
            )
        )
        
        every { repository.getAllReports() } returns flowOf(mockReports)

        viewModel.uiState.test {
            // Initially Loading (or whatever the flow starts with)
            // Due to flatMapLatest, it might trigger immediately or on first emission of _loadTrigger
            
            // Trigger load
            viewModel.loadReports()
            
            val state = awaitItem()
            if (state is SavedReportsUiState.Loading) {
                 val successState = awaitItem() as SavedReportsUiState.Success
                 assertEquals(mockReports, successState.reports)
            } else {
                 val successState = state as SavedReportsUiState.Success
                 assertEquals(mockReports, successState.reports)
            }
        }
    }
}
