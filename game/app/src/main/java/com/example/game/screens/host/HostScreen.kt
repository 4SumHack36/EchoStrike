package com.example.game.screens.host

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.game.screens.game.GameScreen
import com.example.game.screenmodels.ConnectionState
import com.example.game.models.GameMessage
import com.example.game.screenmodels.GameScreenModel
import com.example.game.screenmodels.GameState
import com.example.game.screenmodels.GameTurn
import com.example.game.screenmodels.SoundDirection

class HostScreen : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val gameScreenModel = rememberScreenModel { GameScreenModel(context) }
        var isHosting by remember { mutableStateOf(false) }
        var hostStatus by remember { mutableStateOf("") }
        
        // Observe connection state for navigation
        val connectionState by gameScreenModel.connectionState.collectAsState()
        
        // Navigate to GameScreen when connection is established
        LaunchedEffect(connectionState) {
            if (connectionState is ConnectionState.Connected) {
                navigator.push(GameScreen(gameScreenModel))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Host a Game",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (!isHosting) {
                Button(
                    onClick = {
                        isHosting = true
                        hostStatus = "Starting host..."
                        gameScreenModel.startHosting(context)
                        hostStatus = "Host started successfully!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Start Hosting")
                }
            } else {
                Text(
                    text = hostStatus,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Connection status card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Waiting for client to connect...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
