package com.example.studybloom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.Topic
import com.example.studybloom.data.repository.TopicRepository
import kotlinx.coroutines.launch

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TopicRepository
    private val _topics = MutableLiveData<LiveData<List<Topic>>>()

    var topics: LiveData<List<Topic>> = MutableLiveData()
        private set

    init {
        val topicDao = AppDatabase.getInstance(application).topicDao()
        repository = TopicRepository(topicDao)
    }

    fun loadTopics(subjectId: Int) {
        topics = repository.getTopicsBySubjectId(subjectId)
    }

    fun insert(topic: Topic) = viewModelScope.launch {
        repository.insert(topic)
    }

    fun update(topic: Topic) = viewModelScope.launch {
        repository.update(topic)
    }

    fun delete(topic: Topic) = viewModelScope.launch {
        repository.delete(topic)
    }

    suspend fun getTopicById(id: Int): Topic? {
        return repository.getTopicById(id)
    }

    suspend fun getTotalTopics(subjectId: Int): Int {
        return repository.getTotalTopicsBySubject(subjectId)
    }

    suspend fun getCompletedTopics(subjectId: Int): Int {
        return repository.getCompletedTopicsBySubject(subjectId)
    }
}