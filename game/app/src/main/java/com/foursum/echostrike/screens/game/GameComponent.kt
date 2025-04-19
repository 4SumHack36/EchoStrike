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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foursum.echostrike.R
import com.foursum.echostrike.model.SensorData
import com.foursum.echostrike.util.SoundManager
import com.foursum.echostrike.util.VolumeKeyEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

@Composable
fun GameComponent(
    data: SensorData,
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    val soundManager = remember { SoundManager() }
    val context = LocalContext.current
    // Make LaunchedEffect dependent on isRecording to update when recording state changes
    LaunchedEffect(isRecording) {
        soundManager.loadSound(context, R.raw.opponenthit)
        soundManager.loadSound(context, R.raw.bounce)
        soundManager.loadSound(context, R.raw.slowanotherwhiff)

        soundManager.playSound(R.raw.opponenthit, 0f, 0f)  // silent "warm-up"
        soundManager.playSound(R.raw.bounce, 0f, 0f)
        soundManager.playSound(R.raw.slowanotherwhiff, 0f, 0f)
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
            onClick = {
                val direction = Random(3).nextInt()- 1

                CoroutineScope(Dispatchers.Main).launch {

                    soundManager.playSound(R.raw.opponenthit, 1.0f, 1.0f)  // First sound

                    delay(700L)  // Wait 500 ms

                    soundManager.playSound(R.raw.bounce, 1.0f, 1.0f)    // Second sound

                    delay(400L)  // Another delay

                    soundManager.playSound(R.raw.slowanotherwhiff, 0.2f, 1.0f)
                }


            },
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