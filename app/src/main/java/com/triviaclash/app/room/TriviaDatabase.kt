package com.triviaclash.app.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CachedQuiz::class, RecentMatch::class],
    version = 1,
    exportSchema = false
)
abstract class TriviaDatabase : RoomDatabase() {

    abstract fun quizDao(): QuizDao
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: TriviaDatabase? = null

        fun getDatabase(context: Context): TriviaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TriviaDatabase::class.java,
                    "trivia_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}