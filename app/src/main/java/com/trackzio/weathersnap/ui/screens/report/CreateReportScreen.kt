package com.trackzio.weathersnap.ui.screens.report
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.ui.screens.weather.SharedWeatherViewModel
import com.trackzio.weathersnap.ui.theme.*
import java.io.File

@Composable
fun CreateReportScreen(
    sharedWeatherViewModel: SharedWeatherViewModel,
    capturedImagePath: String?,
    onClearCapturedPath: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateBack: () -> Unit,
    onReportSaved: () -> Unit,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by reportViewModel.uiState.collectAsStateWithLifecycle()
    val weatherFromShared by sharedWeatherViewModel.selectedWeather.collectAsStateWithLifecycle()

    LaunchedEffect(weatherFromShared) {
        weatherFromShared?.let { reportViewModel.initWeatherData(it) }
    }

    val weatherData by reportViewModel.weatherData.collectAsStateWithLifecycle()

    LaunchedEffect(capturedImagePath) {
        if (capturedImagePath != null) {
            reportViewModel.onImageCaptured(capturedImagePath)
            onClearCapturedPath()
        }
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onReportSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        ReportHeader(onBack = onNavigateBack)
        Spacer(modifier = Modifier.height(12.dp))

        weatherData?.let { weather ->
            WeatherSnapshotCard(weather = weather)
            Spacer(modifier = Modifier.height(12.dp))
        }

        PhotoSection(
            imagePath = uiState.capturedImagePath,
            originalKb = uiState.originalSizeKb,
            compressedKb = uiState.compressedSizeKb,
            onCapturePhoto = onNavigateToCamera
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotesSection(
            notes = uiState.notes,
            onNotesChange = reportViewModel::onNotesChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = reportViewModel::saveReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = uiState.capturedImagePath != null && !uiState.isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreenLight)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkBackground, strokeWidth = 2.dp)
            } else {
                Text("Save Report", color = DarkBackground, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReportHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(colors = listOf(Color(0xFF7AAB7C), Color(0xFFB5C96A))))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text("Create Report", color = DarkBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Capture, compress, annotate", color = DarkBackground.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3D2E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back", color = TextPrimary, fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeatherSnapshotCard(weather: WeatherData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(weather.cityName, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(weather.condition, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("${weather.temperature.toInt()}°C", color = AccentGreenLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            val itemModifier = Modifier.weight(1f, fill = false).widthIn(min = 90.dp)
            StatChip("Humidity", "${weather.humidity}%", TealAccent, itemModifier)
            StatChip("Wind", "${weather.windSpeed} m/s", BlueAccent, itemModifier)
            StatChip("Pressure", "${weather.pressure}", OrangeAccent, itemModifier)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(CardDark).padding(10.dp)) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoSection(
    imagePath: String?,
    originalKb: Long,
    compressedKb: Long,
    onCapturePhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 300.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = imagePath,
                transitionSpec = { fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400)) },
                label = "photo_preview"
            ) { path ->
                if (path != null) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Captured photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Photo preview", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCapturePhoto,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreenLight)
        ) {
            Text(
                if (imagePath != null) "Retake Photo" else "Capture Photo",
                color = DarkBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        AnimatedVisibility(visible = imagePath != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    SizeChip("Original", "${originalKb} KB", OrangeAccent, Modifier.weight(1f).widthIn(min = 120.dp))
                    SizeChip("Compressed", "${compressedKb} KB", TealAccent, Modifier.weight(1f).widthIn(min = 120.dp))
                }
            }
        }
    }
}

@Composable
private fun SizeChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(CardDark).padding(12.dp)) {
        Text(label, color = color, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NotesSection(notes: String, onNotesChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Text("Field Notes", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = { Text("Notes", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentGreen
            )
        )
    }
}