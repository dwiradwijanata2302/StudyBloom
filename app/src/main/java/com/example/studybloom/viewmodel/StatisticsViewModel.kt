package com.example.studybloom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.repository.StudySessionRepository
import com.example.studybloom.data.repository.StreakRepository
import com.example.studybloom.utils.StreakCalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository: StudySessionRepository
    private val streakRepository: StreakRepository
    private val context = application

    val allSessions: LiveData<List<*>>

    private val _totalStudyTime = MutableLiveData<String>()
    val totalStudyTime: LiveData<String> = _totalStudyTime

    private val _totalSessions = MutableLiveData<Int>()
    val totalSessions: LiveData<Int> = _totalSessions

    private val _totalTopicsCompleted = MutableLiveData<Int>()
    val totalTopicsCompleted: LiveData<Int> = _totalTopicsCompleted

    private val _currentStreak = MutableLiveData<Int>()
    val currentStreak: LiveData<Int> = _currentStreak

    private val _streakStatus = MutableLiveData<String>()
    val streakStatus: LiveData<String> = _streakStatus

    private val _daysUntilExpire = MutableLiveData<Int>()
    val daysUntilExpire: LiveData<Int> = _daysUntilExpire

    private val _weeklyData = MutableLiveData<Map<String, Int>>()
    val weeklyData: LiveData<Map<String, Int>> = _weeklyData

    init {
        val sessionDao = AppDatabase.getInstance(application).studySessionDao()
        val streakDao = AppDatabase.getInstance(application).streakDao()

        sessionRepository = StudySessionRepository(sessionDao)
        streakRepository = StreakRepository(streakDao)

        allSessions = sessionRepository.getAllSessions

        // Observe changes dan recalculate statistics
        allSessions.observeForever {
            calculateAllStatistics()
        }

        // Initial calculation
        calculateAllStatistics()
    }

    /**
     * Hitung semua statistics
     */
    private fun calculateAllStatistics() {
        viewModelScope.launch {
            // Total study time
            val totalMinutes = sessionRepository.getTotalDuration()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            _totalStudyTime.postValue("${hours}h ${minutes}m")

            // Total sessions
            val sessions = sessionRepository.getTotalSessions()
            _totalSessions.postValue(sessions)

            // Total topics completed
            val completed = sessionRepository.getTotalTopicsCompleted()
            _totalTopicsCompleted.postValue(completed)

            // Current streak
            val streak = StreakCalculator.getCurrentStreak(context)
            _currentStreak.postValue(streak)

            // Streak status
            val status = getStreakStatus()
            _streakStatus.postValue(status)

            // Days until expire
            val daysLeft = StreakCalculator.getDaysUntilExpire(context)
            _daysUntilExpire.postValue(daysLeft)

            // Weekly data
            val weeklyMap = getWeeklyStudyData()
            _weeklyData.postValue(weeklyMap)
        }
    }

    /**
     * Tentukan status streak
     */
    private suspend fun getStreakStatus(): String {
        val lastDate = StreakCalculator.getLastStudyDate(context)
        if (lastDate.isEmpty()) return "Not Started"

        return when {
            StreakCalculator.isStreakExpired(context) -> "Expired"
            StreakCalculator.canRecoverStreak(context) -> "Recoverable"
            else -> "Active"
        }
    }

    /**
     * Hitung study time per hari minggu lalu (7 hari terakhir)
     */
    private suspend fun getWeeklyStudyData(): Map<String, Int> {
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Tue", etc

        val weeklyMap = mutableMapOf<String, Int>()
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        // Init dengan 0
        days.forEach { weeklyMap[it] = 0 }

        // Hitung 7 hari ke belakang
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)
            val dayStr = dayFormat.format(cal.time)

            val sessionsOnDate = allSessions.value?.let {
                it.filterIsInstance<com.example.studybloom.data.entity.StudySession>()
                    .filter { session -> session.sessionDate == dateStr }
                    .sumOf { session -> session.duration }
            } ?: 0

            weeklyMap[dayStr] = sessionsOnDate
        }

        return weeklyMap
    }

    /**
     * Trigger recalculation (manual refresh)
     */
    fun refresh() {
        calculateAllStatistics()
    }
}