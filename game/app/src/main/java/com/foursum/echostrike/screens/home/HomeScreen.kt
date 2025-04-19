package com.foursum.echostrike.screens.home

import android.content.Context
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

data class HomeScreen(private val context: Context): Screen {
    @Composable
    override fun Content() {
        HomeComponent(context)
    }
}