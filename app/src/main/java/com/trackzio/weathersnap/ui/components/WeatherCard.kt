package com.trackzio.weathersnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trackzio.weathersnap.domain.model.WeatherData
import com.trackzio.weathersnap.ui.theme.AccentGreenLight
import com.trackzio.weathersnap.ui.theme.BlueAccent
import com.trackzio.weathersnap.ui.theme.BlueAccentCard
import com.trackzio.weathersnap.ui.theme.CardDark
import com.trackzio.weathersnap.ui.theme.DarkBackground
import com.trackzio.weathersnap.ui.theme.OrangeAccent
import com.trackzio.weathersnap.ui.theme.OrangeAccentCard
import com.trackzio.weathersnap.ui.theme.TealAccent
import com.trackzio.weathersnap.ui.theme.TealAccentCard
import com.trackzio.weathersnap.ui.theme.TextPrimary
import com.trackzio.weathersnap.ui.theme.TextSecondary
import com.trackzio.weathersnap.ui.util.rememberDebouncedClick

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherCard(data: WeatherData, onCreateReport:(() -> Unit)? = null) {
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("city_name")
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            val itemModifier = Modifier
                .weight(1f, fill = false)
                .widthIn(min = 100.dp)
            StatCard(
                label = "Humidity",
                value = "${data.humidity}%",
                valueColor = TealAccent,
                backgroundColor = TealAccentCard,
                modifier = itemModifier
            )
            StatCard(
                label = "Wind",
                value = "${data.windSpeed} m/s",
                valueColor = BlueAccent,
                backgroundColor = BlueAccentCard,
                modifier = itemModifier
            )
            StatCard(
                label = "Pressure",
                value = "${data.pressure}",
                valueColor = OrangeAccent,
                backgroundColor = OrangeAccentCard,
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
                if(onCreateReport!=null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF424338))
                            .padding(12.dp)
                    ) {
                        Text("Report readiness", color = TextSecondary, fontSize = 12.sp)
                        Text("Camera and Room DB enabled", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
        if(onCreateReport!=null) {
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
}
