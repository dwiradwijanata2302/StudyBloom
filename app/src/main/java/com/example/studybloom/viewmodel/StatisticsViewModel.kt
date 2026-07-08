package com.example.studybloom.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.StudySession
import com.example.studybloom.data.entity.Streak
import com.example.studybloom.data.repository.StudySessionRepository
import com.example.studybloom.data.repository.StreakRepository
import com.example.studybloom.utils.StreakCalculator
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val sessionRepository = StudySessionRepository(database.studySessionDao())
    private val streakRepository = StreakRepository(database.streakDao())
    private val context = application

    // Data Sumber
    val allSessions: LiveData<List<StudySession>> = sessionRepository.getAllSessions
    val latestStreak: LiveData<Streak?> = streakRepository.latestStreak

    // Data UI
    private val _totalStudyTime = MutableLiveData<String>("0h 0m")
    val totalStudyTime: LiveData<String> = _totalStudyTime

    private val _totalSessions = MutableLiveData<Int>(0)
    val totalSessions: LiveData<Int> = _totalSessions

    private val _totalTopicsCompleted = MutableLiveData<Int>(0)
    val totalTopicsCompleted: LiveData<Int> = _totalTopicsCompleted

    private val _streakStatus = MutableLiveData<String>("Not Started")
    val streakStatus: LiveData<String> = _streakStatus

    private val _daysUntilExpire = MutableLiveData<Int>(0)
    val daysUntilExpire: LiveData<Int> = _daysUntilExpire

    // 1. Definisi weeklySummary untuk mengatasi unresolved reference
    private val _weeklySummary = MutableLiveData<String>("No study sessions recorded this week.")
    val weeklySummary: LiveData<String> = _weeklySummary

    // Streak Utama
    val currentStreak: LiveData<Int> = latestStreak.map { streak ->
        streak?.streakValue ?: StreakCalculator.getCurrentStreak(context)
    }

    init {
        allSessions.observeForever { sessions ->
            calculateSessionStats(sessions)
        }

        latestStreak.observeForever {
            updateStreakDetails()
        }
    }

    private fun calculateSessionStats(sessions: List<StudySession>?) {
        viewModelScope.launch {
            val totalMinutes = sessionRepository.getTotalDuration()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            _totalStudyTime.postValue("${hours}h ${minutes}m")

            _totalSessions.postValue(sessionRepository.getTotalSessions())
            _totalTopicsCompleted.postValue(sessionRepository.getTotalTopicsCompleted())

            if (!sessions.isNullOrEmpty()) {
                val sevenDaysAgo = LocalDate.now().minusDays(7)
                val weeklyTime = sessions.filter { 
                    try { LocalDate.parse(it.sessionDate).isAfter(sevenDaysAgo) } catch (e: Exception) { false }
                }.sumOf { it.duration }
                
                _weeklySummary.postValue("You studied for $weeklyTime minutes in the last 7 days.")
            }
        }
    }

    private fun updateStreakDetails() {
        viewModelScope.launch {
            // 1. Memanggil fungsi dari StreakCalculator yang sudah dipastikan tersedia
            val isExpired = StreakCalculator.isStreakExpired(context)
            val canRecover = StreakCalculator.canRecoverStreak(context)
            val daysLeft = StreakCalculator.getDaysUntilExpire(context)
            
            _streakStatus.postValue(when {
                isExpired -> "Streak Expired"
                canRecover -> "Recoverable"
                else -> "Active"
            })
            _daysUntilExpire.postValue(daysLeft)
        }
    }
}
