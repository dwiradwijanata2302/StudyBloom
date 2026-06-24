package com.example.studybloom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.studybloom.data.database.AppDatabase
import com.example.studybloom.data.entity.Subject
import com.example.studybloom.data.repository.SubjectRepository
import kotlinx.coroutines.launch

class SubjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubjectRepository
    val allSubjects: LiveData<List<Subject>>

    init {
        val subjectDao = AppDatabase.getInstance(application).subjectDao()
        repository = SubjectRepository(subjectDao)
        allSubjects = repository.getAllSubjects
    }

    fun insert(subject: Subject) = viewModelScope.launch {
        repository.insert(subject)
    }

    fun update(subject: Subject) = viewModelScope.launch {
        repository.update(subject)
    }

    fun delete(subject: Subject) = viewModelScope.launch {
        repository.delete(subject)
    }

    suspend fun getSubjectById(id: Int): Subject? {
        return repository.getSubjectById(id)
    }
}