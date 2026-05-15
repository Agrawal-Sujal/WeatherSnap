package com.trackzio.weathersnap.ui.screens.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.ui.theme.AccentGreen
import com.trackzio.weathersnap.ui.theme.AccentGreenLight
import com.trackzio.weathersnap.ui.theme.BlueAccent
import com.trackzio.weathersnap.ui.theme.BorderColor
import com.trackzio.weathersnap.ui.theme.CardDark
import com.trackzio.weathersnap.ui.theme.DarkBackground
import com.trackzio.weathersnap.ui.theme.OrangeAccent
import com.trackzio.weathersnap.ui.theme.SurfaceDark
import com.trackzio.weathersnap.ui.theme.TealAccent
import com.trackzio.weathersnap.ui.theme.TextPrimary
import com.trackzio.weathersnap.ui.theme.TextSecondary
import com.trackzio.weathersnap.ui.theme.shimmer
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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        WeatherHeader(onReportsClick = rememberDebouncedClick { onNavigateToSavedReports() })

        Spacer(modifier = Modifier.height(12.dp))

        SearchSection(
            query = cityQuery,
            onQueryChange = viewModel::onCityQueryChange,
            onSearch = rememberDebouncedClick { viewModel.fetchWeather() },
            suggestionsState = citySuggestions,
            onCitySelected = rememberDebouncedClickParam { viewModel.onCitySelected(it) }
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
private fun WeatherHeader(onReportsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF3D5A3E), Color(0xFF7AAB7C))
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WeatherSnap",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live weather reports with camera evidence",
                    color = TextPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onReportsClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3D2E)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reports", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    suggestionsState: CitySuggestionState,
    onCitySelected: (CityResult) -> Unit
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
                    unfocusedBorderColor = BorderColor,
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
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
                color = if (suggestionsState is CitySuggestionState.Error) Color(0xFFFF6B6B) else TextSecondary,
                fontSize = 12.sp,
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
private fun SuggestionItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(text = text, color = TextPrimary, fontSize = 14.sp)
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
            is WeatherUiState.Success -> WeatherSuccessState(
                data = weatherState.data,
                onCreateReport = { onCreateReport(weatherState.data) }
            )

            is WeatherUiState.Error -> ErrorState(message = weatherState.message)
            is WeatherUiState.Offline -> OfflineState(onViewSavedReports = onViewSavedReports)
        }
    }
}

@Composable
private fun EmptyWeatherState() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF3D5A3E), Color(0xFF2D5050))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Search. Capture. Save.",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "No weather loaded",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Enter more than 2 letters, choose a city, then search.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmer()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmer()
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops! Something went wrong",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Check your internet connection or try again later.",
            color = Color(0xFFFF6B6B),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

// Offline fallback — shown when IOException is caught (no connectivity)
@Composable
private fun OfflineState(onViewSavedReports: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You're offline",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No internet connection. You can still browse your previously saved reports.",
            color = TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onViewSavedReports,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreenLight)
        ) {
            Text(
                "View Saved Reports",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeatherSuccessState(data: WeatherData, onCreateReport: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.cityName,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = data.condition, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2D4A2D))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${data.temperature.toInt()}°C",
                    color = AccentGreenLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            val itemModifier = Modifier
                .weight(1f, fill = false)
                .widthIn(min = 100.dp)
            WeatherStatCard("Humidity", "${data.humidity}%", TealAccent, itemModifier)
            WeatherStatCard("Wind", "${data.windSpeed} m/s", BlueAccent, itemModifier)
            WeatherStatCard("Pressure", "${data.pressure}", OrangeAccent, itemModifier)
        }

        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth > 400.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Report readiness", color = TextSecondary, fontSize = 13.sp)
                    Text("Camera and Room DB enabled", color = TextPrimary, fontSize = 13.sp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardDark)
                        .padding(12.dp)
                ) {
                    Text("Report readiness", color = TextSecondary, fontSize = 12.sp)
                    Text("Camera and Room DB enabled", color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = rememberDebouncedClick { onCreateReport() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreenLight)
        ) {
            Text(
                "Create Report",
                color = DarkBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun WeatherStatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardDark)
            .padding(10.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}