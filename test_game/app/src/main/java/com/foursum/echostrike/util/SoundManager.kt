package com.foursum.echostrike.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SoundManager {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val soundMap = mutableMapOf<Int, Int>() // resourceId to soundId
    private val _loadedSounds = MutableStateFlow<Set<Int>>(emptySet())
    val loadedSounds = _loadedSounds.asStateFlow()

    init {

        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (status == 0) { // 0 means success
                _loadedSounds.value = _loadedSounds.value + soundId
            }
        }
    }

    fun loadSound(context: Context, resourceId: Int): Int {
        val soundId = soundPool.load(context, resourceId, 1)
        soundMap[resourceId] = soundId
        return soundId
    }

    fun playSound(resourceId: Int, volume: Float = 1.0f, pan: Float = 0.0f): Int {
        val soundId = soundMap[resourceId] ?: return -1

        // Convert pan (-1.0 to 1.0) to left/right volumes
        // pan = -1.0: full left, pan = 0.0: center, pan = 1.0: full right
        val leftVolume = if (pan <= 0) volume else volume * (1 - pan)
        val rightVolume = if (pan >= 0) volume else volume * (1 + pan)

        return soundPool.play(soundId, leftVolume, rightVolume, 1, 0, 1.0f)
    }

    fun stopSound(streamId: Int) {
        soundPool.stop(streamId)
    }

    fun release() {
        soundPool.release()
    }
}