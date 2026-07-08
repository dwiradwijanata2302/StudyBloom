package com.example.studybloom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studybloom.data.entity.Streak

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(streak: Streak)

    @Query("SELECT * FROM streak ORDER BY id DESC")
    fun getAllStreaks(): LiveData<List<Streak>>

    @Query("SELECT * FROM streak WHERE date = :date ORDER BY id DESC LIMIT 1")
    suspend fun getStreakByDate(date: String): Streak?

    /**
     * Ambil streak paling baru berdasarkan ID terakhir yang masuk.
     * Ini memastikan UI selalu mendapatkan angka terbaru.
     */
    @Query("SELECT * FROM streak ORDER BY id DESC LIMIT 1")
    fun getLatestStreakLive(): LiveData<Streak?>

    @Query("SELECT SUM(sessionsCount) FROM streak WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalSessionsInRange(startDate: String, endDate: String): Int?
}
