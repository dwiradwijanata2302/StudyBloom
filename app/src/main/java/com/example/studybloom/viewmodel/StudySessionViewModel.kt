package com.example.studybloom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.StudySession
import com.example.studybloom.data.repository.StudySessionRepository

class StudySessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudySessionRepository
    val allSessions: LiveData<List<StudySession>>

    init {
        val studySessionDao = AppDatabase.getInstance(application).studySessionDao()
        repository = StudySessionRepository(studySessionDao)
        allSessions = repository.getAllSessions
    }

    // PENTING: Gunakan suspend agar proses simpan bisa ditunggu sampai selesai di Activity
    suspend fun insert(session: StudySession) {
        repository.insert(session)
    }

    suspend fun getTotalDuration(): Int {
        return repository.getTotalDuration()
    }

    suspend fun getTotalSessions(): Int {
        return repository.getTotalSessions()
    }

    suspend fun getSessionsCountByDate(date: String): Int {
        return repository.getSessionsCountByDate(date)
    }

    suspend fun getTotalTopicsCompleted(): Int {
        return repository.getTotalTopicsCompleted()
    }
}
