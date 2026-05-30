package com.triviaclash.app.repository

import com.triviaclash.app.models.Quiz
import com.triviaclash.app.models.Question
import com.triviaclash.app.room.CachedQuiz
import com.triviaclash.app.room.QuizDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class QuizRepository(
    private val quizDao: QuizDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ---- LOCAL (Room) ----

    fun getAllCachedQuizzes(): Flow<List<CachedQuiz>> =
        quizDao.getAllCachedQuizzes()

    fun getFavoriteQuizzes(): Flow<List<CachedQuiz>> =
        quizDao.getFavoriteQuizzes()

    fun getQuizzesByCategory(category: String): Flow<List<CachedQuiz>> =
        quizDao.getQuizzesByCategory(category)

    suspend fun getDailyQuiz(): CachedQuiz? =
        quizDao.getDailyQuiz()

    suspend fun toggleFavorite(quiz: CachedQuiz) {
        quizDao.updateQuiz(quiz.copy(isFavorite = !quiz.isFavorite))
    }

    // ---- REMOTE (Firestore) ----

    suspend fun fetchQuizzesFromFirestore(): List<Quiz> {
        return try {
            val snapshot = firestore.collection("quizzes").get().await()
            snapshot.documents.mapNotNull { it.toObject(Quiz::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchQuestionsByQuizId(quizId: String): List<Question> {
        return try {
            val snapshot = firestore.collection("questions")
                .whereEqualTo("quizId", quizId)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Question::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun syncQuizzesToLocal(quizzes: List<Quiz>) {
        val cachedList = quizzes.map {
            CachedQuiz(
                id = it.id,
                title = it.title,
                category = it.category,
                difficulty = it.difficulty,
                questionCount = it.questionCount,
                timePerQuestion = it.timePerQuestion,
                xpReward = it.xpReward,
                coinReward = it.coinReward,
                imageUrl = it.imageUrl,
                isDaily = it.isDaily
            )
        }
        quizDao.insertQuizzes(cachedList)
    }
}