package com.example.game.screens.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.game.screens.host.HostScreen
import com.example.game.screens.join.JoinScreen

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { HomeScreenModel() }

        HomeComponent(
            onHostClick = { navigator.push(HostScreen()) },
            onJoinClick = { navigator.push(JoinScreen()) },
//            onMotionDetectionClick = {}
        )
    }
}
