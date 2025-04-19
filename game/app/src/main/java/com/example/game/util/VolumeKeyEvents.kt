package com.example.game.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object VolumeKeyEvents {
    var volPressed by mutableStateOf(false)
    var onVolumeUpPress: () -> Unit = {}
    var onVolumeUpRelease: () -> Unit = {}
}