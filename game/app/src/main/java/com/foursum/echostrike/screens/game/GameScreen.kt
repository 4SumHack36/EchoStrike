package com.foursum.echostrike.screens.game

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.foursum.echostrike.util.SoundManager

data class GameScreen(
    private val context: Context,
): Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { GameScreenModel(context) }
        val data by model.sensorData.collectAsState()
        val isRecording by model.isRecording.collectAsState()

        GameComponent(data, isRecording ,onToggleRecording = { model.toggleRecording() })
    }
}