package com.example.studybloom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studybloom.data.entity.StudySession

@Dao
interface StudySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySession)

    @Query("SELECT * FROM study_session ORDER BY sessionDate DESC")
    fun getAllSessions(): LiveData<List<StudySession>>

    @Query("SELECT * FROM study_session WHERE sessionDate = :date")
    suspend fun getSessionsByDate(date: String): List<StudySession>

    @Query("SELECT SUM(duration) FROM study_session")
    suspend fun getTotalDuration(): Int?

    @Query("SELECT COUNT(*) FROM study_session")
    suspend fun getTotalSessions(): Int

    @Query("SELECT COUNT(*) FROM study_session WHERE sessionDate = :date")
    suspend fun getSessionsCountByDate(date: String): Int

    @Query("SELECT COUNT(DISTINCT topic.id) FROM topic WHERE completed = 1")
    suspend fun getTotalTopicsCompleted(): Int
}