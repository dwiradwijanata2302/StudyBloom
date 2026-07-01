package com.example.studybloom.data.repository

import androidx.lifecycle.LiveData
import com.example.studybloom.data.dao.StreakDao
import com.example.studybloom.data.entity.Streak

class StreakRepository(private val streakDao: StreakDao) {

    val allStreaks: LiveData<List<Streak>> = streakDao.getAllStreaks()

    suspend fun insert(streak: Streak) {
        streakDao.insert(streak)
    }

    suspend fun getStreakByDate(date: String): Streak? {
        return streakDao.getStreakByDate(date)
    }

    suspend fun getLatestStreak(): Streak? {
        return streakDao.getLatestStreak()
    }

    suspend fun getTotalSessionsInRange(startDate: String, endDate: String): Int {
        return streakDao.getTotalSessionsInRange(startDate, endDate) ?: 0
    }
}