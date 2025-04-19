package com.example.game

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.game.screens.home.HomeScreen
import android.view.KeyEvent
import com.example.game.util.VolumeKeyEvents

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanChatTheme {
                Navigator(HomeScreen()) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    VolumeKeyEvents.onVolumeUpPress()
                    return true
                }
                KeyEvent.ACTION_UP -> {
                    VolumeKeyEvents.onVolumeUpRelease()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
