package com.triviaclash.app.repository

import com.triviaclash.app.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUser get() = auth.currentUser

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        username: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("No UID"))
            val user = User(
                uid = uid,
                username = username,
                email = email
            )
            firestore.collection("users").document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginAnonymously(): Result<Unit> {
        return try {
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid ?: return Result.failure(Exception("No UID"))
            val docRef = firestore.collection("users").document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                val user = User(
                    uid = uid,
                    username = "Guest",
                    email = ""
                )
                docRef.set(user).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }

    suspend fun updateUserStats(
        uid: String,
        xpToAdd: Int,
        coinsToAdd: Int,
        score: Int,
        correct: Int,
        total: Int,
        quizTitle: String,
        category: String
    ) {
        try {
            val userRef = firestore.collection("users").document(uid)
            var username = "Guest"
            var newXP = 0
            var newLevel = 1
            var newStreak = 0

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentXP = snapshot.getLong("xp")?.toInt() ?: 0
                val currentCoins = snapshot.getLong("coins")?.toInt() ?: 0
                val currentGames = snapshot.getLong("totalGames")?.toInt() ?: 0
                val currentHighScore = snapshot.getLong("highestScore")?.toInt() ?: 0
                val currentStreak = snapshot.getLong("streak")?.toInt() ?: 0
                val lastPlayedAt = snapshot.getLong("lastPlayedAt") ?: 0L
                username = snapshot.getString("username") ?: "Guest"
                newXP = currentXP + xpToAdd
                newLevel = (newXP / 500) + 1
                val newHighScore = maxOf(currentHighScore, score)
                val now = System.currentTimeMillis()

                // Streak логика
                newStreak = when {
                    lastPlayedAt == 0L -> 1
                    isSameDay(lastPlayedAt, now) -> currentStreak
                    isYesterday(lastPlayedAt) -> currentStreak + 1
                    else -> 1
                }

                val currentTotalCorrect = snapshot.getLong("totalCorrect")?.toInt() ?: 0
                val currentTotalQuestions = snapshot.getLong("totalQuestions")?.toInt() ?: 0

                transaction.update(userRef, mapOf(
                    "xp" to newXP,
                    "level" to newLevel,
                    "coins" to currentCoins + coinsToAdd,
                    "totalGames" to currentGames + 1,
                    "highestScore" to newHighScore,
                    "streak" to newStreak,
                    "lastPlayedAt" to now,
                    "totalCorrect" to currentTotalCorrect + correct,
                    "totalQuestions" to currentTotalQuestions + total
                ))

            }.await()

            // Зачувај match history
            val matchData = hashMapOf(
                "quizTitle" to quizTitle,
                "category" to category,
                "score" to score,
                "correctAnswers" to correct,
                "totalQuestions" to total,
                "xpEarned" to xpToAdd,
                "coinsEarned" to coinsToAdd,
                "playedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(uid)
                .collection("match_history")
                .add(matchData)
                .await()

            // Ажурирај Leaderboard
            val leaderboardData = hashMapOf(
                "uid" to uid,
                "username" to username,
                "xp" to newXP,
                "level" to newLevel,
                "weeklyScore" to score
            )
            firestore.collection("leaderboard")
                .document(uid)
                .set(leaderboardData)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout() = auth.signOut()
}