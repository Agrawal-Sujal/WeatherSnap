package com.trackzio.weathersnap.ui.screens.report
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackzio.weathersnap.data.local.entity.WeatherReportEntity
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.domain.repository.WeatherRepository
import com.trackzio.weathersnap.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ReportUiState(
    val capturedImagePath: String? = null,
    val originalSizeKb: Long = 0L,
    val compressedSizeKb: Long = 0L,
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val imageCompressor: ImageCompressor,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // WeatherData is passed via SavedStateHandle so it survives process death.
    // We store it as individual primitives because SavedStateHandle requires Parcelable/primitive types.
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()

    private val _uiState = MutableStateFlow(ReportUiState(
        capturedImagePath = savedStateHandle["draft_image_path"],
        originalSizeKb = savedStateHandle["draft_original_kb"] ?: 0L,
        compressedSizeKb = savedStateHandle["draft_compressed_kb"] ?: 0L,
        notes = savedStateHandle["draft_notes"] ?: ""
    ))
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun initWeatherData(data: WeatherData) {
        // Only set once — never overwrite with a freshly fetched result
        if (_weatherData.value == null) {
            _weatherData.value = data
        }
    }

    fun onImageCaptured(rawImagePath: String) {
        viewModelScope.launch {
            val rawFile = File(rawImagePath)
            val result = imageCompressor.compress(context, rawFile)

            // Clean up old draft image if any
            val oldPath = _uiState.value.capturedImagePath
            if (oldPath != null && oldPath != rawImagePath) {
                File(oldPath).delete()
            }

            val newState = _uiState.value.copy(
                capturedImagePath = result.compressedFile.absolutePath,
                originalSizeKb = result.originalSizeKb,
                compressedSizeKb = result.compressedSizeKb
            )
            _uiState.value = newState

            // Persist draft to survive process death
            savedStateHandle["draft_image_path"] = result.compressedFile.absolutePath
            savedStateHandle["draft_original_kb"] = result.originalSizeKb
            savedStateHandle["draft_compressed_kb"] = result.compressedSizeKb
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
        savedStateHandle["draft_notes"] = notes
    }

    fun saveReport() {
        val weather = _weatherData.value ?: return
        val imagePath = _uiState.value.capturedImagePath ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val report = WeatherReportEntity(
                    cityName = weather.cityName,
                    temperature = weather.temperature,
                    condition = weather.condition,
                    humidity = weather.humidity,
                    windSpeed = weather.windSpeed,
                    pressure = weather.pressure,
                    imagePath = imagePath,
                    originalSizeKb = _uiState.value.originalSizeKb,
                    compressedSizeKb = _uiState.value.compressedSizeKb,
                    notes = _uiState.value.notes
                )
                repository.saveReport(report)
                clearDraft()
                _uiState.value = _uiState.value.copy(isSaving = false, savedSuccessfully = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save report"
                )
            }
        }
    }

    private fun clearDraft() {
        savedStateHandle.remove<String>("draft_image_path")
        savedStateHandle.remove<Long>("draft_original_kb")
        savedStateHandle.remove<Long>("draft_compressed_kb")
        savedStateHandle.remove<String>("draft_notes")
    }

    override fun onCleared() {
        super.onCleared()
        // If saved successfully, clean up the temp raw file
        // The compressed file is kept since it was saved to DB
    }
}