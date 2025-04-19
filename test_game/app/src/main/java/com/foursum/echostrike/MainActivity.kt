package com.foursum.echostrike

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.foursum.echostrike.screens.home.HomeScreen
import com.foursum.echostrike.ui.theme.EchoStrikeTheme
import com.foursum.echostrike.util.VolumeKeyEvents

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EchoStrikeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(Modifier.padding(innerPadding))
                        Navigator(HomeScreen(this@MainActivity)) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
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
//                    VolumeKeyEvents.volPressed = true
                }

                KeyEvent.ACTION_UP -> {
                    VolumeKeyEvents.onVolumeUpRelease()
                    return true
//                    VolumeKeyEvents.volPressed = false
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
//   TODO: ("Delete this Composable comment later")
//
//    @Composable
//    fun VolumeUpKeyListener() {
//
//        LaunchedEffect(Unit) {
//            VolumeKeyEvents.onVolumeUpPress = {
//                volUpPressed = true
//            }
//            VolumeKeyEvents.onVolumeUpRelease = {
//                volUpPressed = false
//            }
//        }
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(text = "Volume up Pressed: $volUpPressed", fontSize = 24.sp)
//        }
//    }
}