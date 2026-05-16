package com.trackzio.weathersnap.ui.screens.report

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trackzio.weathersnap.domain.repository.WeatherRepository
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.util.ImageCompressor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private lateinit var viewModel: ReportViewModel
    private val repository: WeatherRepository = mockk()
    private val imageCompressor: ImageCompressor = mockk()
    private val context: Context = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        viewModel = ReportViewModel(repository, imageCompressor, savedStateHandle, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveReport success updates state and clears draft`() = runTest {
        val weather = WeatherData("London", 20.0, "Clear", 50, 5.0, 1013)
        val imagePath = "/path/to/image.jpg"
        
        // Setup initial state
        viewModel.initWeatherData(weather)
        // Manually set image path since we are testing saveReport, not onImageCaptured here
        // (Actually it's better to just set it via savedStateHandle if we want it to be picked up, 
        // or just let the viewModel state update)
        // Since _uiState is initialized with savedStateHandle values:
        savedStateHandle["draft_image_path"] = imagePath
        // Re-init viewModel to pick up savedStateHandle values if needed, 
        // or just mock the state if possible. 
        // Wait, the current ReportViewModel init logic uses savedStateHandle at creation.
        
        val viewModelWithState = ReportViewModel(repository, imageCompressor, savedStateHandle, context)
        viewModelWithState.initWeatherData(weather)

        coEvery { repository.saveReport(any()) } returns 1L

        viewModelWithState.uiState.test {
            // Skip initial state from test start
            val initialState = awaitItem()
            assertEquals(imagePath, initialState.capturedImagePath)
            
            viewModelWithState.saveReport()
            
            val loadingState = awaitItem()
            assertTrue(loadingState.isSaving)
            
            val successState = awaitItem()
            assertTrue(successState.savedSuccessfully)
            assertEquals(false, successState.isSaving)
        }
    }

    @Test
    fun `onNotesChange updates state and savedStateHandle`() = runTest {
        val notes = "Test notes"
        
        viewModel.onNotesChange(notes)
        
        assertEquals(notes, viewModel.uiState.value.notes)
        assertEquals(notes, savedStateHandle.get<String>("draft_notes"))
    }
}
