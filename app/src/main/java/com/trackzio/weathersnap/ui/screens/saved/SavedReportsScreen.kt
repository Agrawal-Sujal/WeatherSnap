package com.trackzio.weathersnap.ui.screens.saved

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.trackzio.weathersnap.data.local.entity.WeatherReportEntity
import com.trackzio.weathersnap.ui.components.AppHeader
import com.trackzio.weathersnap.ui.components.EmptyState
import com.trackzio.weathersnap.ui.components.StatCard
import com.trackzio.weathersnap.ui.theme.AccentGreen
import com.trackzio.weathersnap.ui.theme.AccentGreenLight
import com.trackzio.weathersnap.ui.theme.CardDark
import com.trackzio.weathersnap.ui.theme.DarkBackground
import com.trackzio.weathersnap.ui.theme.OrangeAccent
import com.trackzio.weathersnap.ui.theme.OrangeAccentCard
import com.trackzio.weathersnap.ui.theme.SurfaceDark
import com.trackzio.weathersnap.ui.theme.TealAccent
import com.trackzio.weathersnap.ui.theme.TealAccentCard
import com.trackzio.weathersnap.ui.theme.TextPrimary
import com.trackzio.weathersnap.ui.theme.TextSecondary
import com.trackzio.weathersnap.ui.util.rememberDebouncedClick
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavedReportsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadReports()
    }
    SavedReportsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun SavedReportsScreenContent(
    uiState: SavedReportsUiState,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            when (val state = uiState) {
                is SavedReportsUiState.Loading -> {
                    AppHeader(
                        title = "Saved Reports",
                        subtitle = "0 reports stored locally",
                        buttonText = "Back",
                        onButtonClick = rememberDebouncedClick { onNavigateBack() }
                    )
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentGreen)
                    }
                }

                is SavedReportsUiState.Success -> {
                    AppHeader(
                        title = "Saved Reports",
                        subtitle = "${state.reports.size} report${if (state.reports.size != 1) "s" else ""} stored locally",
                        buttonText = "Back",
                        onButtonClick = rememberDebouncedClick { onNavigateBack() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedContent(
                        targetState = state.reports.isEmpty(),
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "SavedReportsContent"
                    ) { isEmpty ->
                        if (isEmpty) {
                            EmptyState(
                                title = "No reports yet",
                                description = "Search for a city and create your first report"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.reports, key = { it.id }) { report ->
                                    ReportCard(report = report)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportCard(report: WeatherReportEntity) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(report.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
    ) {
        // Image
        AsyncImage(
            model = File(report.imagePath),
            contentDescription = "Report image",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.cityName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(report.condition, color = TextSecondary, fontSize = 13.sp)
                    Text(formattedDate, color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2D4A2D))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "${report.temperature.toInt()}°C",
                        color = AccentGreenLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                StatCard(
                    label = "Original",
                    value = "${report.originalSizeKb} KB",
                    valueColor = OrangeAccent,
                    backgroundColor = OrangeAccentCard,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 120.dp)
                )
                StatCard(
                    label = "Compressed",
                    value = "${report.compressedSizeKb} KB",
                    valueColor = TealAccent,
                    backgroundColor = TealAccentCard,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 120.dp)
                )
            }

            if (report.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF424338))
                        .padding(10.dp)
                ) {
                    Text(report.notes, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
