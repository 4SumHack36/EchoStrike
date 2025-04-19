package com.foursum.echostrikesoundplayer.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foursum.echostrikesoundplayer.util.SoundManager
import com.foursum.echostrikesoundplayer.R

@Composable
fun HomeComponent() {
    val context = LocalContext.current
    var volume by remember { mutableStateOf(0.8f) }
    var pan by remember { mutableStateOf(0.0f) }

    // Create and remember the SoundManager instance
    val soundManager = remember { SoundManager() }

    // Load a sound when the component is first created
    LaunchedEffect(Unit) {
        soundManager.loadSound(context, R.raw.sound)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "EchoStrike Sound Player", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Text("Volume: ${String.format("%.2f", volume)}")
        Slider(
            value = volume,
            onValueChange = { volume = it },
            valueRange = 0f..1f,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Pan: ${String.format("%.2f", pan)}")
        Slider(
            value = pan,
            onValueChange = { pan = it },
            valueRange = -1f..1f,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                soundManager.playSound(R.raw.sound, volume, -1.0f)
            }) {
                Text("Left")
            }

            Button(onClick = {
                soundManager.playSound(R.raw.sound, volume, 0.0f)
            }) {
                Text("Center")
            }

            Button(onClick = {
                soundManager.playSound(R.raw.sound, volume, 1.0f)
            }) {
                Text("Right")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                soundManager.playSound(R.raw.sound, volume, pan)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Play Sound with Current Settings")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset buttons for quick selection
        Text("Presets:", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = {
                volume = 0.3f
                pan = 0.0f
            }) {
                Text("Quiet")
            }

            OutlinedButton(onClick = {
                volume = 1.0f
                pan = 0.0f
            }) {
                Text("Loud")
            }

            OutlinedButton(onClick = {
                volume = 0.8f
                pan = -0.7f
            }) {
                Text("Left")
            }

            OutlinedButton(onClick = {
                volume = 0.8f
                pan = 0.7f
            }) {
                Text("Right")
            }
        }
    }

    // Clean up resources when the component is disposed
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }
}