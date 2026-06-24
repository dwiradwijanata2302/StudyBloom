package com.example.studybloom.data.repository

import androidx.lifecycle.LiveData
import com.example.studybloom.data.dao.StudySessionDao
import com.example.studybloom.data.entity.StudySession

class StudySessionRepository(private val studySessionDao: StudySessionDao) {

    val getAllSessions: LiveData<List<StudySession>> = studySessionDao.getAllSessions()

    suspend fun insert(session: StudySession) {
        studySessionDao.insert(session)
    }

    suspend fun getSessionsByDate(date: String): List<StudySession> {
        return studySessionDao.getSessionsByDate(date)
    }

    suspend fun getTotalDuration(): Int {
        return studySessionDao.getTotalDuration() ?: 0
    }

    suspend fun getTotalSessions(): Int {
        return studySessionDao.getTotalSessions()
    }

    suspend fun getSessionsCountByDate(date: String): Int {
        return studySessionDao.getSessionsCountByDate(date)
    }

    suspend fun getTotalTopicsCompleted(): Int {
        return studySessionDao.getTotalTopicsCompleted()
    }
}