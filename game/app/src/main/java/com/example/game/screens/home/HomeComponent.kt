package com.example.game.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeComponent(
    onHostClick: () -> Unit,
    onJoinClick: () -> Unit,
//    onMotionDetectionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EchoStrike",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onHostClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            Text(text = "Host a Game", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onJoinClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            Text(text = "Join a Game", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Button(
//            onClick = onMotionDetectionClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.tertiary
//            )
//        ) {
//            Text("Motion Detection")
//        }
    }
}
