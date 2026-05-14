package com.trackzio.weathersnap.ui.screens.weather
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackzio.weathersnap.domain.model.CityResult
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.ui.theme.*

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
        // Header
        WeatherHeader(onReportsClick = onNavigateToSavedReports)

        Spacer(modifier = Modifier.height(12.dp))

        // Search section
        SearchSection(
            query = cityQuery,
            onQueryChange = viewModel::onCityQueryChange,
            onSearch = viewModel::fetchWeather,
            suggestionsState = citySuggestions,
            onCitySelected = viewModel::onCitySelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weather content
        WeatherContent(
            weatherState = weatherState,
            onCreateReport = { data -> onNavigateToReport(data) }
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D3D2E)
                ),
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
            OutlinedTextField(
                value = query,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreenLight
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Search", color = DarkBackground, fontWeight = FontWeight.SemiBold)
            }
        }

        // Search feedback text
        AnimatedVisibility(
            visible = query.length <= 2 || suggestionsState is CitySuggestionState.Empty,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val feedbackText = if (query.length <= 2) {
                "Enter more than 2 letters to start city suggestions."
            } else {
                "No cities found. Try adding more letters or check spelling."
            }
            Text(
                text = feedbackText,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Suggestions
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
    onCreateReport: (WeatherData) -> Unit
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
            Text("No weather loaded", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
        // City Name and Condition Shimmer
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

        // Stat Cards Shimmer
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        // Readiness Bar Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmer()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button Shimmer
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $message",
            color = Color(0xFFFF6B6B),
            fontSize = 14.sp
        )
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
                Text(
                    text = data.condition,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
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

            WeatherStatCard(
                label = "Humidity",
                value = "${data.humidity}%",
                valueColor = TealAccent,
                modifier = itemModifier
            )
            WeatherStatCard(
                label = "Wind",
                value = "${data.windSpeed} m/s",
                valueColor = BlueAccent,
                modifier = itemModifier
            )
            WeatherStatCard(
                label = "Pressure",
                value = "${data.pressure}",
                valueColor = OrangeAccent,
                modifier = itemModifier
            )
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
            onClick = onCreateReport,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreenLight
            )
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