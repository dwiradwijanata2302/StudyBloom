package com.example.studybloom.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.Streak
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object StreakCalculator {

    private const val PREFS_NAME = "studybloom_streak"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_LAST_STUDY_DATE = "last_study_date"
    private const val STREAK_EXPIRY_DAYS = 4

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Hitung gap antara dua tanggal (dalam hari)
     */
    private fun getGapDays(lastDate: String, currentDate: String): Int {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val last = formatter.parse(lastDate) ?: return 999
            val current = formatter.parse(currentDate) ?: return 999
            val diffMs = current.time - last.time
            TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        } catch (e: Exception) {
            999 // error handling
        }
    }

    /**
     * Ambil tanggal hari ini dalam format yyyy-MM-dd
     */
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Cek apakah streak sudah expired (gap >= 4 hari)
     */
    fun isStreakExpired(context: Context): Boolean {
        val prefs = getPreferences(context)
        val lastDate = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""

        if (lastDate.isEmpty()) return false

        val gap = getGapDays(lastDate, getTodayDateString())
        return gap >= STREAK_EXPIRY_DAYS
    }

    /**
     * Cek apakah streak bisa dipulihkan (1-3 hari gap)
     */
    fun canRecoverStreak(context: Context): Boolean {
        val prefs = getPreferences(context)
        val lastDate = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""

        if (lastDate.isEmpty()) return false

        val gap = getGapDays(lastDate, getTodayDateString())
        return gap > 0 && gap < STREAK_EXPIRY_DAYS
    }

    /**
     * Hitung berapa hari tersisa sebelum streak expired
     */
    fun getDaysUntilExpire(context: Context): Int {
        val prefs = getPreferences(context)
        val lastDate = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""

        if (lastDate.isEmpty()) return 0

        val gap = getGapDays(lastDate, getTodayDateString())
        val daysLeft = STREAK_EXPIRY_DAYS - gap
        return maxOf(0, daysLeft)
    }

    /**
     * Update streak saat session selesai
     */
    fun updateStreak(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val today = getTodayDateString()
            val prefs = getPreferences(context)
            val lastDate = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""
            var currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)

            // Jika sudah belajar hari ini, skip
            if (lastDate == today) {
                return@launch
            }

            // Hitung gap
            val gap = if (lastDate.isEmpty()) 0 else getGapDays(lastDate, today)

            val db = AppDatabase.getInstance(context)
            val sessionDao = db.studySessionDao()

            // Hitung berapa session hari ini
            val sessionCountToday = sessionDao.getSessionsCountByDate(today)

            // Logic streak
            when {
                gap >= STREAK_EXPIRY_DAYS -> {
                    // Streak expired, reset
                    currentStreak = 0
                    if (sessionCountToday == 1) {
                        currentStreak = 1 // Start fresh
                    }
                }
                gap > 0 && gap < STREAK_EXPIRY_DAYS -> {
                    // Streak recoverable, add based on session count
                    when {
                        sessionCountToday >= 4 -> {
                            currentStreak += 2 // +1 recovery + 1 bonus
                        }
                        sessionCountToday == 1 -> {
                            currentStreak += 1 // +1 recovery
                        }
                        // else: +0 (tunggu sampai 4 sessions)
                    }
                }
                gap == 0 -> {
                    // Same day, tidak bisa terjadi karena check di atas
                }
            }

            // Save to SharedPreferences
            prefs.edit().apply {
                putInt(KEY_CURRENT_STREAK, currentStreak)
                putString(KEY_LAST_STUDY_DATE, today)
                apply()
            }

            // Save history ke database (optional)
            val streak = Streak(
                date = today,
                streakValue = currentStreak,
                sessionsCount = sessionCountToday,
                hasBonus = sessionCountToday >= 4
            )
            db.streakDao().insert(streak)
        }
    }

    /**
     * Ambil current streak value (dengan check expiry)
     */
    fun getCurrentStreak(context: Context): Int {
        val prefs = getPreferences(context)
        val lastDate = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""

        if (lastDate.isEmpty()) return 0

        val gap = getGapDays(lastDate, getTodayDateString())

        // Jika expired, reset ke 0
        if (gap >= STREAK_EXPIRY_DAYS) {
            prefs.edit().putInt(KEY_CURRENT_STREAK, 0).apply()
            return 0
        }

        return prefs.getInt(KEY_CURRENT_STREAK, 0)
    }

    /**
     * Get last study date
     */
    fun getLastStudyDate(context: Context): String {
        val prefs = getPreferences(context)
        return prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""
    }

    /**
     * Reset streak manually (optional)
     */
    fun resetStreak(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            putInt(KEY_CURRENT_STREAK, 0)
            putString(KEY_LAST_STUDY_DATE, "")
            apply()
        }
    }
}