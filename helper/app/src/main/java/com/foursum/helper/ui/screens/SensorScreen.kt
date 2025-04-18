package com.foursum.helper.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.foursum.helper.models.SensorData
import com.foursum.helper.ui.screenmodel.SensorScreenModel
import com.foursum.helper.util.SharedPrefsManager


class SensorScreen(
    private val context: Context,
    private val sharedPrefsManager: SharedPrefsManager
) : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { SensorScreenModel(sharedPrefsManager, context) }
        val data by model.sensorData.collectAsState()
        val isRecording by model.isRecording.collectAsState()

        SensorScreenContent(data, isRecording, onToggleRecording = { model.toggleRecording() })
    }

    @Composable
    private fun SensorScreenContent(
        data: SensorData,
        isRecording: Boolean,
        onToggleRecording: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//            RecordingIndicator(isRecording)
            SensorRow("Linear Acceleration", data.formatValues(data.linearAcceleration))
            SensorRow("Gyroscope", data.formatValues(data.gyroscope))
            SensorRow("Magnetometer", data.formatValues(data.magnetometer))

            SaveComponent(
                context = context,
                sharedPrefsManager = sharedPrefsManager
            )

//            Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom
            Button(
                onClick = onToggleRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(0.dp))
                    .weight(1f), // Make the button cover one-third of the screen height
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.Green
                )
            ) {
                Text(
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    color = if (isRecording) Color.White else Color.Black,
                    fontSize = 36.sp
                )
            }
        }
    }

    @Composable
    private fun RecordingIndicator(isRecording: Boolean) {
        Box(
            modifier = Modifier
                .size(100.dp) // Increased size for better visibility
                .background(
                    if (isRecording) Color.Red else Color.Green,
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRecording) "STOP" else "START", // Updated text to reflect recording state
                color = if (isRecording) Color.White else Color.Black,
                style = MaterialTheme.typography.labelMedium // Adjusted typography for better readability
            )
        }
    }

    @Composable
    private fun SensorRow(label: String, formattedValues: String) {
        Column {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(formattedValues)
        }
    }
}
