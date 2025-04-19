package com.foursum.echostrike.screens.menu

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
import com.foursum.echostrike.screens.join.JoinScreen
import com.foursum.echostrike.screens.host.HostScreen


@Composable
fun MenuComponent() {
    val navigator = LocalNavigator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // HOST Button - Long Press to navigate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            navigator?.push(HostScreen())
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HOST",
                color = Color(0xFF00FF00),
                fontSize = 36.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // JOIN Button - Long Press to navigate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            navigator?.push(JoinScreen())
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "JOIN",
                color = Color(0xFFFF0033),
                fontSize = 36.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
