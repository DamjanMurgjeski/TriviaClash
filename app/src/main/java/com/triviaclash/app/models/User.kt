package com.triviaclash.app.models

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val coins: Int = 0,
    val streak: Int = 0,
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val highestScore: Int = 0,
    val achievements: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)