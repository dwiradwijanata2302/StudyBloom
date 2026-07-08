package com.example.studybloom.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.Topic
import com.example.studybloom.data.repository.TopicRepository
import kotlinx.coroutines.launch

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TopicRepository
    
    // Gunakan subjectId sebagai trigger
    private val _currentSubjectId = MutableLiveData<Int>()

    // topics akan otomatis update setiap kali _currentSubjectId berubah
    val topics: LiveData<List<Topic>> = _currentSubjectId.switchMap { id ->
        repository.getTopicsBySubjectId(id)
    }

    init {
        val topicDao = AppDatabase.getInstance(application).topicDao()
        repository = TopicRepository(topicDao)
    }

    fun loadTopics(subjectId: Int) {
        _currentSubjectId.value = subjectId
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
