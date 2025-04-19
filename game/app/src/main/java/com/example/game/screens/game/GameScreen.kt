package com.example.game.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.game.screenmodels.GameScreenModel
import com.example.game.screenmodels.GameState
import com.example.game.screenmodels.GameTurn
import com.example.game.util.VolumeKeyEvents

class GameScreen(private val gameScreenModel: GameScreenModel) : Screen {
    @Composable
    override fun Content() {
        val gameState by gameScreenModel.gameState.collectAsState()
        val gameTurn by gameScreenModel.gameTurn.collectAsState()
        val isRecording by gameScreenModel.isRecording.collectAsState()
        val currentSoundDirection by gameScreenModel.currentSoundDirection.collectAsState()
        val isHost = remember { mutableStateOf(gameScreenModel.isHost()) }
        LaunchedEffect(isRecording) {
            VolumeKeyEvents.onVolumeUpPress = {
                if (!isRecording) {
                    gameScreenModel.toggleRecording();
                }
            }
            VolumeKeyEvents.onVolumeUpRelease = {
                if (isRecording) {
                    gameScreenModel.toggleRecording();
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Game Title
                Text(
                    text = "EchoStrike",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Game state display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (gameState) {
                            GameState.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                            GameState.GAME_OVER -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Game Status: ${gameState.name.replace('_', ' ')}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sound direction indicator
                currentSoundDirection?.let { soundDir ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Play Sound",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "PLAY SOUND",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )

                            Text(
                                text = soundDir.displayName.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Disconnect button
                OutlinedButton(
                    onClick = { gameScreenModel.disconnect() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("DISCONNECT")
                }
//                OutlinedButton(
//                    onClick = { gameScreenModel.disconnect() },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp)
//                ) {
//                    Text("RECORD")
//                }
                Spacer(modifier = Modifier.padding(24.dp))
                Button(
                    onClick = { gameScreenModel.toggleRecording() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(0.dp)),
//                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color.Red else Color.Green
                    )
                ) {
                    Text(
                        text = if (isRecording) "Stop Recording" else "Start Recording",
                        color = if (isRecording) Color.White else Color.Black,
                        fontSize = 36.sp
                    )
                }
            }
        }
    }
}
