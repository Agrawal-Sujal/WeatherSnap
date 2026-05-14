package com.trackzio.weathersnap.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.ui.screens.report.CreateReportScreenContent
import com.trackzio.weathersnap.ui.screens.report.ReportUiState
import com.trackzio.weathersnap.ui.screens.saved.SavedReportsScreenContent
import com.trackzio.weathersnap.ui.screens.saved.SavedReportsUiState
import com.trackzio.weathersnap.ui.screens.weather.CitySuggestionState
import com.trackzio.weathersnap.ui.screens.weather.WeatherScreenContent
import com.trackzio.weathersnap.ui.screens.weather.WeatherUiState
import org.junit.Rule
import org.junit.Test

class WeatherSnapUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weatherScreen_displaysEmptyState_initially() {
        composeTestRule.setContent {
            WeatherScreenContent(
                cityQuery = "",
                weatherState = WeatherUiState.Idle,
                citySuggestions = CitySuggestionState.Idle,
                onCityQueryChange = {},
                onSearch = {},
                onCitySelected = {},
                onNavigateToReport = {},
                onNavigateToSavedReports = {}
            )
        }

        composeTestRule.onNodeWithText("No weather loaded").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search. Capture. Save.").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_displaysWeatherData_whenSuccess() {
        val weatherData = WeatherData("London", 20.0, "Sunny", 40, 5.0, 1012)
        
        composeTestRule.setContent {
            WeatherScreenContent(
                cityQuery = "London",
                weatherState = WeatherUiState.Success(weatherData),
                citySuggestions = CitySuggestionState.Idle,
                onCityQueryChange = {},
                onSearch = {},
                onCitySelected = {},
                onNavigateToReport = {},
                onNavigateToSavedReports = {}
            )
        }

        composeTestRule.onNode(hasTestTag("city_name")).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunny").assertIsDisplayed()
        composeTestRule.onNodeWithText("20°C").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Report").assertIsDisplayed()
    }

    @Test
    fun savedReportsScreen_displaysEmptyState_whenNoReports() {
        composeTestRule.setContent {
            SavedReportsScreenContent(
                uiState = SavedReportsUiState.Success(emptyList()),
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("No reports yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search for a city and create your first report").assertIsDisplayed()
    }

    @Test
    fun createReportScreen_displaysWeatherAndPhotoPreview() {
        val weatherData = WeatherData("London", 20.0, "Sunny", 40, 5.0, 1012)
        
        composeTestRule.setContent {
            CreateReportScreenContent(
                uiState = ReportUiState(),
                weatherData = weatherData,
                onNavigateBack = {},
                onNavigateToCamera = {},
                onNotesChange = {},
                onSaveReport = {}
            )
        }

        composeTestRule.onNodeWithText("London").assertIsDisplayed()
        composeTestRule.onNodeWithText("Photo preview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Capture Photo").assertIsDisplayed()
    }

    @Test
    fun weatherScreen_searchTriggersCallback() {
        var searchClicked = false
        composeTestRule.setContent {
            WeatherScreenContent(
                cityQuery = "London",
                weatherState = WeatherUiState.Idle,
                citySuggestions = CitySuggestionState.Idle,
                onCityQueryChange = {},
                onSearch = { searchClicked = true },
                onCitySelected = {},
                onNavigateToReport = {},
                onNavigateToSavedReports = {}
            )
        }

        composeTestRule.onNodeWithText("Search").performClick()
        assert(searchClicked)
    }
}
