package com.example.studybloom.data.repository

import androidx.lifecycle.LiveData
import com.example.studybloom.data.dao.TopicDao
import com.example.studybloom.data.entity.Topic

class TopicRepository(private val topicDao: TopicDao) {

    fun getTopicsBySubjectId(subjectId: Int): LiveData<List<Topic>> {
        return topicDao.getTopicsBySubjectId(subjectId)
    }

    suspend fun insert(topic: Topic) {
        topicDao.insert(topic)
    }

    suspend fun update(topic: Topic) {
        topicDao.update(topic)
    }

    suspend fun delete(topic: Topic) {
        topicDao.delete(topic)
    }

    suspend fun getTopicById(id: Int): Topic? {
        return topicDao.getTopicById(id)
    }

    suspend fun getTotalTopicsBySubject(subjectId: Int): Int {
        return topicDao.getTotalTopicsBySubject(subjectId)
    }

    suspend fun getCompletedTopicsBySubject(subjectId: Int): Int {
        return topicDao.getCompletedTopicsBySubject(subjectId)
    }
}