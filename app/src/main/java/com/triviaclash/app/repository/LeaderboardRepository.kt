package com.triviaclash.app.repository

import com.triviaclash.app.models.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class LeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getGlobalLeaderboard(): List<LeaderboardEntry> {
        return try {
            val snapshot = firestore.collection("leaderboard")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(100)
                .get().await()
            snapshot.documents.mapNotNull {
                it.toObject(LeaderboardEntry::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWeeklyLeaderboard(): List<LeaderboardEntry> {
        return try {
            val snapshot = firestore.collection("leaderboard")
                .orderBy("weeklyScore", Query.Direction.DESCENDING)
                .limit(100)
                .get().await()
            snapshot.documents.mapNotNull {
                it.toObject(LeaderboardEntry::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateLeaderboardEntry(entry: LeaderboardEntry) {
        try {
            firestore.collection("leaderboard")
                .document(entry.uid)
                .set(entry)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}