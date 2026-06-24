package com.example.studybloom.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studybloom.data.entity.Topic

@Dao
interface TopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: Topic)

    @Update
    suspend fun update(topic: Topic)

    @Delete
    suspend fun delete(topic: Topic)

    @Query("SELECT * FROM topic WHERE subjectId = :subjectId ORDER BY name ASC")
    fun getTopicsBySubjectId(subjectId: Int): LiveData<List<Topic>>

    @Query("SELECT * FROM topic WHERE id = :id")
    suspend fun getTopicById(id: Int): Topic?

    @Query("SELECT COUNT(*) FROM topic WHERE subjectId = :subjectId")
    suspend fun getTotalTopicsBySubject(subjectId: Int): Int

    @Query("SELECT COUNT(*) FROM topic WHERE subjectId = :subjectId AND completed = 1")
    suspend fun getCompletedTopicsBySubject(subjectId: Int): Int
}