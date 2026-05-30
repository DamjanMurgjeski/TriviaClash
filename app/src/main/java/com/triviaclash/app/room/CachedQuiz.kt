package com.triviaclash.app.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_quizzes")
data class CachedQuiz(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val questionCount: Int,
    val timePerQuestion: Int,
    val xpReward: Int,
    val coinReward: Int,
    val imageUrl: String,
    val isDaily: Boolean,
    val isFavorite: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)