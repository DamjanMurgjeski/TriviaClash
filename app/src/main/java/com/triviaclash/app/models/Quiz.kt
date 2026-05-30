package com.triviaclash.app.models

data class Quiz(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val difficulty: String = "medium",
    val questionCount: Int = 10,
    val timePerQuestion: Int = 30,
    val xpReward: Int = 100,
    val coinReward: Int = 50,
    val imageUrl: String = "",
    val isDaily: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)