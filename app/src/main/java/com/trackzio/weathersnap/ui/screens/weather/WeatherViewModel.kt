package com.trackzio.weathersnap.ui.screens.weather
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackzio.weathersnap.data.WeatherRepository
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class CitySuggestionState {
    object Idle : CitySuggestionState()
    object Loading : CitySuggestionState()
    data class Success(val cities: List<CityResult>) : CitySuggestionState()
    object Empty : CitySuggestionState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _cityQuery = MutableStateFlow("")
    val cityQuery: StateFlow<String> = _cityQuery.asStateFlow()

    private val _selectedCity = MutableStateFlow<CityResult?>(null)
    val selectedCity: StateFlow<CityResult?> = _selectedCity.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _citySuggestions = MutableStateFlow<CitySuggestionState>(CitySuggestionState.Idle)
    val citySuggestions: StateFlow<CitySuggestionState> = _citySuggestions.asStateFlow()

    private var searchJob: Job? = null

    fun onCityQueryChange(query: String) {
        _cityQuery.value = query
        _selectedCity.value = null

        if (query.length <= 2) {
            _citySuggestions.value = CitySuggestionState.Idle
            searchJob?.cancel()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _citySuggestions.value = CitySuggestionState.Loading
            try {
                val results = repository.searchCities(query)
                _citySuggestions.value = if (results.isEmpty()) {
                    CitySuggestionState.Empty
                } else {
                    CitySuggestionState.Success(results)
                }
            } catch (e: Exception) {
                _citySuggestions.value = CitySuggestionState.Idle
            }
        }
    }

    fun onCitySelected(city: CityResult) {
        _selectedCity.value = city
        _cityQuery.value = city.displayName
        _citySuggestions.value = CitySuggestionState.Idle
    }

    fun fetchWeather() {
        val city = _selectedCity.value ?: return
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            try {
                val weather = repository.getWeather(city.latitude, city.longitude, city.displayName)
                _weatherState.value = WeatherUiState.Success(weather)
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(e.message ?: "Failed to fetch weather")
            }
        }
    }

    fun getLoadedWeather(): WeatherData? =
        (_weatherState.value as? WeatherUiState.Success)?.data
}