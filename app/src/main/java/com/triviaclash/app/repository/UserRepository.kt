package com.triviaclash.app.repository

import com.triviaclash.app.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentXP = snapshot.getLong("xp")?.toInt() ?: 0
                val currentCoins = snapshot.getLong("coins")?.toInt() ?: 0
                val currentGames = snapshot.getLong("totalGames")?.toInt() ?: 0
                val currentHighScore = snapshot.getLong("highestScore")?.toInt() ?: 0
                val newXP = currentXP + xpToAdd
                val newLevel = (newXP / 500) + 1
                val newHighScore = maxOf(currentHighScore, score)

                transaction.update(userRef, mapOf(
                    "xp" to newXP,
                    "level" to newLevel,
                    "coins" to currentCoins + coinsToAdd,
                    "totalGames" to currentGames + 1,
                    "highestScore" to newHighScore
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

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout() = auth.signOut()
}