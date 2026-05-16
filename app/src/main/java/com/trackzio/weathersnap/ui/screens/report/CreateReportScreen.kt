package com.trackzio.weathersnap.ui.screens.report

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.trackzio.weathersnap.ui.components.AppHeader
import com.trackzio.weathersnap.ui.components.StatCard
import com.trackzio.weathersnap.ui.components.WeatherCard
import com.trackzio.weathersnap.ui.screens.weather.SharedWeatherViewModel
import com.trackzio.weathersnap.ui.theme.AccentGreen
import com.trackzio.weathersnap.ui.theme.AccentGreenLight
import com.trackzio.weathersnap.ui.theme.BorderColor
import com.trackzio.weathersnap.ui.theme.CardDark
import com.trackzio.weathersnap.ui.theme.DarkBackground
import com.trackzio.weathersnap.ui.theme.OrangeAccent
import com.trackzio.weathersnap.ui.theme.OrangeAccentCard
import com.trackzio.weathersnap.ui.theme.SurfaceDark
import com.trackzio.weathersnap.ui.theme.TealAccent
import com.trackzio.weathersnap.ui.theme.TealAccentCard
import com.trackzio.weathersnap.ui.theme.TextPrimary
import com.trackzio.weathersnap.ui.theme.TextSecondary
import com.trackzio.weathersnap.ui.theme.White
import com.trackzio.weathersnap.ui.util.rememberDebouncedClick
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
        AppHeader(
            title = "Create Report",
            subtitle = "Capture, compress, annotate",
            buttonText = "Back",
            onButtonClick = rememberDebouncedClick { onNavigateBack() }
        )
        Spacer(modifier = Modifier.height(12.dp))

        weatherData?.let { weather ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
            ) {
                WeatherCard(data = weather)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        PhotoSection(
            imagePath = uiState.capturedImagePath,
            originalKb = uiState.originalSizeKb,
            compressedKb = uiState.compressedSizeKb,
            onCapturePhoto = rememberDebouncedClick { onNavigateToCamera() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotesSection(
            notes = uiState.notes,
            onNotesChange = reportViewModel::onNotesChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = rememberDebouncedClick { reportViewModel.saveReport() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = uiState.capturedImagePath != null && !uiState.isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreenLight)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DarkBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Save Report",
                    color = DarkBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
                .background( Brush.horizontalGradient(
                    colors = listOf(Color(0xFF444638), Color(0xFF3E4416))
                )),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = imagePath,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                        animationSpec = tween(400)
                    )
                },
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

        AnimatedVisibility(
            visible = imagePath != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    StatCard(
                        label = "Original",
                        value = "${originalKb} KB",
                        valueColor = OrangeAccent,
                        backgroundColor = OrangeAccentCard,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 120.dp)
                            .padding(end = 8.dp)
                    )

                    StatCard(
                        label = "Compressed",
                        value = "${compressedKb} KB",
                        valueColor = TealAccent,
                        backgroundColor = TealAccentCard,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 120.dp)
                    )
                }
            }
        }
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
            label = { Text("Notes", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = White,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentGreen
            )
        )
    }
}
