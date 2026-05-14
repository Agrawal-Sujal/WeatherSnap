package com.trackzio.weathersnap.ui.navigation
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trackzio.weathersnap.ui.screens.camera.CameraScreen
import com.trackzio.weathersnap.ui.screens.report.CreateReportScreen
import com.trackzio.weathersnap.ui.screens.saved.SavedReportsScreen
import com.trackzio.weathersnap.ui.screens.weather.SharedWeatherViewModel
import com.trackzio.weathersnap.ui.screens.weather.WeatherScreen

sealed class Screen(val route: String) {
    object Weather : Screen("weather")
    object CreateReport : Screen("create_report")
    object Camera : Screen("camera")
    object SavedReports : Screen("saved_reports")
}

@Composable
fun WeatherSnapNavGraph(
    navController: NavHostController = rememberNavController(),
    // Activity-scoped: hiltViewModel() at this level uses the Activity as ViewModelStoreOwner
    sharedWeatherViewModel: SharedWeatherViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Weather.route) {
            WeatherScreen(
                onNavigateToReport = { weather ->
                    sharedWeatherViewModel.setWeather(weather)
                    navController.navigate(Screen.CreateReport.route)
                },
                onNavigateToSavedReports = { navController.navigate(Screen.SavedReports.route) }
            )
        }

        composable(Screen.CreateReport.route) {
            val capturedPath = it.savedStateHandle
                .getStateFlow<String?>("captured_image_path", null)
                .collectAsState()

            CreateReportScreen(
                sharedWeatherViewModel = sharedWeatherViewModel,
                capturedImagePath = capturedPath.value,
                onClearCapturedPath = {
                    it.savedStateHandle.remove<String>("captured_image_path")
                },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateBack = { navController.popBackStack() },
                onReportSaved = {
                    sharedWeatherViewModel.clearWeather()
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route)
                    }
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { imagePath ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_path", imagePath)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.SavedReports.route) {
            SavedReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}