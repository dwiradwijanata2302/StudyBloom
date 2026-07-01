package com.example.studybloom.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studybloom.data.dao.StreakDao
import com.example.studybloom.data.dao.StudySessionDao
import com.example.studybloom.data.dao.SubjectDao
import com.example.studybloom.data.dao.TopicDao
import com.example.studybloom.data.entity.StudySession
import com.example.studybloom.data.entity.Subject
import com.example.studybloom.data.entity.Topic

@Database(
    entities = [Subject::class, Topic::class, StudySession::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studybloom_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}