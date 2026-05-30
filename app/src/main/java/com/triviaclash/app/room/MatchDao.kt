package com.triviaclash.app.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Query("SELECT * FROM recent_matches ORDER BY playedAt DESC")
    fun getAllMatches(): Flow<List<RecentMatch>>

    @Query("SELECT * FROM recent_matches ORDER BY playedAt DESC LIMIT 10")
    fun getRecentMatches(): Flow<List<RecentMatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: RecentMatch)

    @Query("DELETE FROM recent_matches")
    suspend fun deleteAllMatches()
}