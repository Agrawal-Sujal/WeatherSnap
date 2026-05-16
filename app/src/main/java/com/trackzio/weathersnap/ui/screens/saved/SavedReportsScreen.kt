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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.trackzio.weathersnap.data.local.WeatherReportEntity
import com.trackzio.weathersnap.ui.theme.AccentGreenLight
import com.trackzio.weathersnap.ui.theme.AccentGreen
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
                    SavedReportsHeader(count = 0, onBack = rememberDebouncedClick { onNavigateBack() })
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentGreen)
                    }
                }
                is SavedReportsUiState.Success -> {
                    SavedReportsHeader(count = state.reports.size, onBack = rememberDebouncedClick { onNavigateBack() })

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedContent(
                        targetState = state.reports.isEmpty(),
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "SavedReportsContent"
                    ) { isEmpty ->
                        if (isEmpty) {
                            EmptyState()
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

@Composable
private fun SavedReportsHeader(count: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7AAB7C), Color(0xFFB5C96A))
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                "Saved Reports",
                color = DarkBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$count report${if (count != 1) "s" else ""} stored locally",
                color = DarkBackground.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
        Button(
            onClick = rememberDebouncedClick { onBack() },
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3D2E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back", color = TextPrimary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No reports yet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Search for a city and create your first report", color = TextSecondary, fontSize = 13.sp)
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
                    Text(report.cityName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                SizeInfoChip("Original", "${report.originalSizeKb} KB", OrangeAccent,
                    OrangeAccentCard, Modifier.weight(1f).widthIn(min = 120.dp))
                SizeInfoChip("Compressed", "${report.compressedSizeKb} KB", TealAccent,
                    TealAccentCard, Modifier.weight(1f).widthIn(min = 120.dp))
            }

            if (report.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardDark)
                        .padding(10.dp)
                ) {
                    Text(report.notes, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SizeInfoChip(label: String, value: String, color: Color,background:Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(12.dp)
    ) {
        Text(label, color = TextPrimary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}