package com.example.studybloom.data.repository

import androidx.lifecycle.LiveData
import com.example.studybloom.data.dao.StreakDao
import com.example.studybloom.data.entity.Streak

class StreakRepository(private val streakDao: StreakDao) {

    // Ambil streak terbaru sebagai LiveData agar UI otomatis update
    val latestStreak: LiveData<Streak?> = streakDao.getLatestStreakLive()

    suspend fun insert(streak: Streak) {
        streakDao.insert(streak)
    }

    suspend fun getStreakByDate(date: String): Streak? {
        return streakDao.getStreakByDate(date)
    }
}
