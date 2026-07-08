package com.example.studybloom.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.Streak
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Helper untuk menghitung streak belajar menggunakan java.time.LocalDate.
 */
object StreakCalculator {

    private const val PREFS_NAME = "studybloom_streak_prefs"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_LAST_STUDY_DATE = "last_study_date"
    private const val KEY_BONUS_CLAIMED_DATE = "bonus_claimed_date"
    private const val STREAK_EXPIRY_DAYS = 4 // Bolos maksimal 4 hari

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getToday(): LocalDate = LocalDate.now()

    /**
     * Cek apakah streak sudah hangus (lebih dari 4 hari tidak belajar)
     */
    fun isStreakExpired(context: Context): Boolean {
        val lastDateStr = getPreferences(context).getString(KEY_LAST_STUDY_DATE, "") ?: ""
        if (lastDateStr.isEmpty()) return false
        val lastDate = LocalDate.parse(lastDateStr)
        return ChronoUnit.DAYS.between(lastDate, getToday()) >= STREAK_EXPIRY_DAYS
    }

    /**
     * Cek apakah streak masih bisa diselamatkan (gap 2-3 hari)
     */
    fun canRecoverStreak(context: Context): Boolean {
        val lastDateStr = getPreferences(context).getString(KEY_LAST_STUDY_DATE, "") ?: ""
        if (lastDateStr.isEmpty()) return false
        val lastDate = LocalDate.parse(lastDateStr)
        val gap = ChronoUnit.DAYS.between(lastDate, getToday())
        return gap in 2 until STREAK_EXPIRY_DAYS
    }

    /**
     * Hitung sisa hari sebelum streak hangus
     */
    fun getDaysUntilExpire(context: Context): Int {
        val lastDateStr = getPreferences(context).getString(KEY_LAST_STUDY_DATE, "") ?: ""
        if (lastDateStr.isEmpty()) return 0
        val lastDate = LocalDate.parse(lastDateStr)
        val gap = ChronoUnit.DAYS.between(lastDate, getToday()).toInt()
        return maxOf(0, STREAK_EXPIRY_DAYS - gap)
    }

    /**
     * Update streak saat session selesai.
     */
    suspend fun updateStreak(context: Context) {
        val today = getToday()
        val todayStr = today.toString()
        val prefs = getPreferences(context)
        
        val lastDateStr = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""
        var currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val bonusClaimedDate = prefs.getString(KEY_BONUS_CLAIMED_DATE, "") ?: ""

        val db = AppDatabase.getInstance(context)
        val sessionCountToday = db.studySessionDao().getSessionsCountByDate(todayStr)

        // 1. LOGIKA UTAMA (Harian)
        if (lastDateStr != todayStr) {
            if (lastDateStr.isEmpty()) {
                currentStreak = 1
            } else {
                val lastDate = LocalDate.parse(lastDateStr)
                val gap = ChronoUnit.DAYS.between(lastDate, today)

                if (gap >= 1) {
                    if (gap >= STREAK_EXPIRY_DAYS) {
                        currentStreak = 1 
                    } else {
                        currentStreak += 1 
                    }
                }
            }
            prefs.edit().putString(KEY_LAST_STUDY_DATE, todayStr).apply()
        }

        if (sessionCountToday >= 1 && currentStreak == 0) {
            currentStreak = 1
        }

        // 2. LOGIKA BONUS: Sesi ke-4 hari ini dapat bonus +1 streak
        if (sessionCountToday >= 4 && bonusClaimedDate != todayStr) {
            currentStreak += 1
            prefs.edit().putString(KEY_BONUS_CLAIMED_DATE, todayStr).apply()
        }

        // 3. SIMPAN HASIL KE PREFS
        prefs.edit().putInt(KEY_CURRENT_STREAK, currentStreak).apply()

        // 4. SYNC KE DATABASE
        val streakRecord = Streak(
            date = todayStr,
            streakValue = currentStreak,
            sessionsCount = sessionCountToday,
            hasBonus = sessionCountToday >= 4
        )
        db.streakDao().insert(streakRecord)
    }

    /**
     * Ambil current streak value
     */
    fun getCurrentStreak(context: Context): Int {
        val prefs = getPreferences(context)
        val lastDateStr = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""
        
        if (lastDateStr.isEmpty()) return 0
        
        val lastDate = LocalDate.parse(lastDateStr)
        if (ChronoUnit.DAYS.between(lastDate, getToday()) >= STREAK_EXPIRY_DAYS) {
            prefs.edit().putInt(KEY_CURRENT_STREAK, 0).apply()
            return 0
        }
        return prefs.getInt(KEY_CURRENT_STREAK, 0)
    }
}
