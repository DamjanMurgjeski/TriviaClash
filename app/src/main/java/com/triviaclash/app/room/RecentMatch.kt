package com.triviaclash.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_matches")
data class RecentMatch(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val quizId: String,
    val quizTitle: String,
    val category: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val playedAt: Long = System.currentTimeMillis()
)