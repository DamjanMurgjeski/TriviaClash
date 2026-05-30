package com.triviaclash.app.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query("SELECT * FROM cached_quizzes")
    fun getAllCachedQuizzes(): Flow<List<CachedQuiz>>

    @Query("SELECT * FROM cached_quizzes WHERE isFavorite = 1")
    fun getFavoriteQuizzes(): Flow<List<CachedQuiz>>

    @Query("SELECT * FROM cached_quizzes WHERE category = :category")
    fun getQuizzesByCategory(category: String): Flow<List<CachedQuiz>>

    @Query("SELECT * FROM cached_quizzes WHERE isDaily = 1 LIMIT 1")
    suspend fun getDailyQuiz(): CachedQuiz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: CachedQuiz)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<CachedQuiz>)

    @Update
    suspend fun updateQuiz(quiz: CachedQuiz)

    @Delete
    suspend fun deleteQuiz(quiz: CachedQuiz)

    @Query("DELETE FROM cached_quizzes")
    suspend fun deleteAllQuizzes()
}