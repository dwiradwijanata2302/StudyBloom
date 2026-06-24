package com.example.studybloom.data.repository

import androidx.lifecycle.LiveData
import com.example.studybloom.data.dao.SubjectDao
import com.example.studybloom.data.entity.Subject

class SubjectRepository(private val subjectDao: SubjectDao) {

    val getAllSubjects: LiveData<List<Subject>> = subjectDao.getAllSubjects()

    suspend fun insert(subject: Subject) {
        subjectDao.insert(subject)
    }

    suspend fun update(subject: Subject) {
        subjectDao.update(subject)
    }

    suspend fun delete(subject: Subject) {
        subjectDao.delete(subject)
    }

    suspend fun getSubjectById(id: Int): Subject? {
        return subjectDao.getSubjectById(id)
    }
}