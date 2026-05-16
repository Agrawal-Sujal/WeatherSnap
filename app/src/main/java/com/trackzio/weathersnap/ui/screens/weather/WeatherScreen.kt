package com.trackzio.weathersnap.ui.screens.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.ui.components.AppHeader
import com.trackzio.weathersnap.ui.components.EmptyWeatherState
import com.trackzio.weathersnap.ui.components.ErrorState
import com.trackzio.weathersnap.ui.components.LoadingState
import com.trackzio.weathersnap.ui.components.OfflineState
import com.trackzio.weathersnap.ui.components.SuggestionItem
import com.trackzio.weathersnap.ui.components.WeatherCard
import com.trackzio.weathersnap.ui.theme.AccentGreen
import com.trackzio.weathersnap.ui.theme.AccentGreenLight
import com.trackzio.weathersnap.ui.theme.BorderColor
import com.trackzio.weathersnap.ui.theme.DarkBackground
import com.trackzio.weathersnap.ui.theme.SurfaceDark
import com.trackzio.weathersnap.ui.theme.TextPrimary
import com.trackzio.weathersnap.ui.theme.TextSecondary
import com.trackzio.weathersnap.ui.theme.White
import com.trackzio.weathersnap.ui.util.rememberDebouncedClick
import com.trackzio.weathersnap.ui.util.rememberDebouncedClickParam

@Composable
fun WeatherScreen(
    onNavigateToReport: (WeatherData) -> Unit,
    onNavigateToSavedReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val cityQuery by viewModel.cityQuery.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val citySuggestions by viewModel.citySuggestions.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    WeatherScreenContent(
        cityQuery = cityQuery,
        weatherState = weatherState,
        citySuggestions = citySuggestions,
        onCityQueryChange = viewModel::onCityQueryChange,
        onSearch = viewModel::fetchWeather,
        onCitySelected = viewModel::onCitySelected,
        onNavigateToReport = onNavigateToReport,
        onNavigateToSavedReports = onNavigateToSavedReports,
        selectedCity = selectedCity
    )
}

@Composable
fun WeatherScreenContent(
    cityQuery: String,
    weatherState: WeatherUiState,
    citySuggestions: CitySuggestionState,
    onCityQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCitySelected: (CityResult) -> Unit,
    onNavigateToReport: (WeatherData) -> Unit,
    onNavigateToSavedReports: () -> Unit,
    selectedCity: CityResult?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        AppHeader(
            title = "WeatherSnap",
            subtitle = "Live weather reports with camera evidence",
            buttonText = "Reports",
            onButtonClick = rememberDebouncedClick { onNavigateToSavedReports() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SearchSection(
            query = cityQuery,
            onQueryChange = onCityQueryChange,
            onSearch = rememberDebouncedClick { onSearch() },
            suggestionsState = citySuggestions,
            onCitySelected = rememberDebouncedClickParam { onCitySelected(it) },
            selectedCity = selectedCity
        )

        Spacer(modifier = Modifier.height(12.dp))

        WeatherContent(
            weatherState = weatherState,
            onCreateReport = { data -> onNavigateToReport(data) },
            onViewSavedReports = onNavigateToSavedReports
        )
    }
}

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    suggestionsState: CitySuggestionState,
    onCitySelected: (CityResult) -> Unit,
    selectedCity: CityResult?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val displayQuery =
                if (suggestionsState is CitySuggestionState.Loading && query.isEmpty()) "---" else query
            OutlinedTextField(
                value = displayQuery,
                onValueChange = onQueryChange,
                label = { Text("City", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (suggestionsState is CitySuggestionState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AccentGreen,
                            strokeWidth = 2.dp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = TextPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentGreen,
                    focusedLabelColor = AccentGreen,
                    unfocusedLabelColor = TextSecondary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onSearch,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreenLight),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                enabled = selectedCity != null && suggestionsState !is CitySuggestionState.Loading
            ) {
                Text("Search", color = DarkBackground, fontWeight = FontWeight.SemiBold)
            }
        }

        AnimatedVisibility(
            visible = query.length <= 2 || suggestionsState is CitySuggestionState.Empty || suggestionsState is CitySuggestionState.Error,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val feedbackText = when {
                query.length <= 2 -> "Enter more than 2 letters to start city suggestions."
                suggestionsState is CitySuggestionState.Error -> "Unable to load suggestions. Check your internet connection."
                else -> "No cities found. Try adding more letters or check spelling."
            }
            Text(
                text = feedbackText,
                color = if (suggestionsState is CitySuggestionState.Error) Color(0xFFFF6B6B) else White,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        AnimatedVisibility(
            visible = suggestionsState is CitySuggestionState.Success,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val cities = (suggestionsState as? CitySuggestionState.Success)?.cities ?: emptyList()
            Column(modifier = Modifier.padding(top = 8.dp)) {
                cities.forEach { city ->
                    SuggestionItem(
                        text = city.displayName,
                        onClick = { onCitySelected(city) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(
    weatherState: WeatherUiState,
    onCreateReport: (WeatherData) -> Unit,
    onViewSavedReports: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
    ) {
        when (weatherState) {
            is WeatherUiState.Idle -> EmptyWeatherState()
            is WeatherUiState.Loading -> LoadingState()
            is WeatherUiState.Success -> WeatherCard(
                data = weatherState.data,
                onCreateReport = { onCreateReport(weatherState.data) }
            )

            is WeatherUiState.Error -> ErrorState(message = weatherState.message)
            is WeatherUiState.Offline -> OfflineState(onViewSavedReports = onViewSavedReports)
        }
    }
}
