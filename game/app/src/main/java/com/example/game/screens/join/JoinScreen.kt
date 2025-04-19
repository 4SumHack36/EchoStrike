package com.example.game.screens.join

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.game.models.GameMessage
import com.example.game.screenmodels.ConnectionState
import com.example.game.screenmodels.GameScreenModel
import com.example.game.screenmodels.GameState
import com.example.game.screenmodels.GameTurn
class JoinScreen : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val gameScreenModel = rememberScreenModel { GameScreenModel(context) }
        val availableHosts by gameScreenModel.availableHosts.collectAsState()
        val isScanning by gameScreenModel.isScanning.collectAsState()
        val connectionState by gameScreenModel.connectionState.collectAsState()
        val errorMessage by gameScreenModel.errorMessage.collectAsState()
        
        // Navigate to GameScreen when connection is established
        LaunchedEffect(connectionState) {
            if (connectionState is ConnectionState.Connected) {
                navigator.push(GameScreen(gameScreenModel))
            }
        }
        
        // Snackbar host state for showing errors
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Show error messages in Snackbar
        LaunchedEffect(errorMessage) {
            errorMessage?.let {
                snackbarHostState.showSnackbar(
                    message = it,
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Long
                )
                gameScreenModel.resetErrorMessage()
            }
        }

        LaunchedEffect(Unit) {
            gameScreenModel.startDiscovery(context)
        }

        DisposableEffect(Unit) {
            onDispose {
                gameScreenModel.stopDiscovery()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
            ) {
                Text(
                    text = when (connectionState) {
                        is ConnectionState.Connected -> "Connected"
                        is ConnectionState.Connecting -> "Connecting..."
                        else -> "Available Hosts"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Connection state handler
                when (connectionState) {
                    is ConnectionState.Connecting -> {
                        // Show connecting indicator
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Connecting to host...")
                            }
                        }
                    }
                    
                    is ConnectionState.Connected -> {
                        // Show connecting indicator until navigation happens
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                    text = "Connected to host! Starting game...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    
                    else -> {
                        // Show available hosts for joining
                        if (isScanning) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (availableHosts.isEmpty() && !isScanning) {
                            Text(
                                text = "No hosts found. Make sure the host is online and on the same network.",
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn {
                                items(availableHosts) { host ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = host.serviceName)
                                            Button(onClick = {
                                                gameScreenModel.connectToHost(host)
                                            }) {
                                                Text("Join")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = { gameScreenModel.startDiscovery(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }
        }
    }
    
}
