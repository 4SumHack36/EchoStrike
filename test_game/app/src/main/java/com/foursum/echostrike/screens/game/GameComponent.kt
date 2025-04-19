package com.foursum.echostrike.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foursum.echostrike.model.SensorData
import com.foursum.echostrike.util.VolumeKeyEvents

@Composable
fun GameComponent(
    data: SensorData,
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    // Make LaunchedEffect dependent on isRecording to update when recording state changes
    LaunchedEffect(isRecording) {
        VolumeKeyEvents.onVolumeUpPress = {
            if (!isRecording) {
                onToggleRecording()
            }
        }
        VolumeKeyEvents.onVolumeUpRelease = {
            if (isRecording) {
                onToggleRecording()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onToggleRecording,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(0.dp))
                .weight(1f),
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