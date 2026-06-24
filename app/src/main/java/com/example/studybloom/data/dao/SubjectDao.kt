package com.example.studybloom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studybloom.data.entity.Subject

@Dao
interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject)

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("SELECT * FROM subject ORDER BY name ASC")
    fun getAllSubjects(): LiveData<List<Subject>>

    @Query("SELECT * FROM subject WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?
}