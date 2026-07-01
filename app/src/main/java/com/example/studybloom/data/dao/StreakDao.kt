package com.example.studybloom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studybloom.data.entity.Streak

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(streak: Streak)

    @Query("SELECT * FROM streak ORDER BY date DESC")
    fun getAllStreaks(): LiveData<List<Streak>>

    @Query("SELECT * FROM streak WHERE date = :date")
    suspend fun getStreakByDate(date: String): Streak?

    @Query("SELECT * FROM streak ORDER BY date DESC LIMIT 1")
    suspend fun getLatestStreak(): Streak?

    @Query("SELECT SUM(sessionsCount) FROM streak WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalSessionsInRange(startDate: String, endDate: String): Int?
}