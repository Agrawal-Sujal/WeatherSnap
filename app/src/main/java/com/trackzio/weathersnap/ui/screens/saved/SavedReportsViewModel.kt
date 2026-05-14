package com.trackzio.weathersnap.ui.screens.saved
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackzio.weathersnap.data.WeatherRepository
import com.trackzio.weathersnap.data.local.WeatherReportEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    repository: WeatherRepository
) : ViewModel() {

    val reports: StateFlow<List<WeatherReportEntity>> = repository.getAllReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}