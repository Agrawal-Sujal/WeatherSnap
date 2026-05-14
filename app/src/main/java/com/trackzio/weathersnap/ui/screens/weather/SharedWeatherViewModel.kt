package com.trackzio.weathersnap.ui.screens.weather
import androidx.lifecycle.ViewModel
import com.trackzio.weathersnap.domain.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Activity-scoped ViewModel that holds the weather snapshot selected
 * when the user enters the Create Report flow. This ensures:
 * 1. The exact weather at time of selection is preserved (not re-fetched).
 * 2. The snapshot survives navigation to camera and back.
 * 3. The snapshot is accessible to ReportViewModel without re-fetching.
 */
@HiltViewModel
class SharedWeatherViewModel @Inject constructor() : ViewModel() {

    private val _selectedWeather = MutableStateFlow<WeatherData?>(null)
    val selectedWeather: StateFlow<WeatherData?> = _selectedWeather.asStateFlow()

    fun setWeather(weather: WeatherData) {
        _selectedWeather.value = weather
    }

    fun clearWeather() {
        _selectedWeather.value = null
    }
}