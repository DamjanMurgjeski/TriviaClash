package com.triviaclash.app.models

data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val xpReward: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0
)