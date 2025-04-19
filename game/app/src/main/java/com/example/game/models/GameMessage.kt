package com.example.game.models

data class GameMessage(
    val content: String,
    val senderName: String,
    val isFromMe: Boolean
)
