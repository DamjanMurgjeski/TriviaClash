package com.triviaclash.app.models

data class LeaderboardEntry(
    val uid: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val score: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val rank: Int = 0,
    val weeklyScore: Int = 0
)