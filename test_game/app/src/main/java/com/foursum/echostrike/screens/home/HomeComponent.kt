package com.foursum.echostrike.screens.home

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.foursum.echostrike.screens.game.GameScreen
import com.foursum.echostrike.screens.menu.MenuScreen

@Composable
fun HomeComponent(context: Context) {
    val navigator = LocalNavigator.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020202))
    ) {

        Text(
            text = "EchoStrike",
            color = Color(0xFF00FFFF),
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            // Play Area — Long Press Only
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                navigator?.push(GameScreen(context))
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PLAY",
                    color = Color(0xFF00FF00),
                    fontSize = 60.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Exit Area — Long Press Only
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                (context as Activity).finishAffinity()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EXIT",
                    color = Color(0xFFFF0033),
                    fontSize = 60.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
