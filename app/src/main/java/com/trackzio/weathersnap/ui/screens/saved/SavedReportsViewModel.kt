package com.trackzio.weathersnap.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackzio.weathersnap.data.local.entity.WeatherReportEntity
import com.trackzio.weathersnap.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class SavedReportsUiState {
    object Loading : SavedReportsUiState()
    data class Success(val reports: List<WeatherReportEntity>) : SavedReportsUiState()
}

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _loadTrigger = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SavedReportsUiState> = _loadTrigger
        .flatMapLatest { shouldLoad ->
            if (shouldLoad) {
                repository.getAllReports().map { SavedReportsUiState.Success(it) }
            } else {
                MutableStateFlow(SavedReportsUiState.Loading)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SavedReportsUiState.Loading
        )

    fun loadReports() {
        _loadTrigger.value = true
    }
}
