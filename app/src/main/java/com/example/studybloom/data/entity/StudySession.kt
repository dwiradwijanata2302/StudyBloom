package com.example.studybloom.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_session",
    foreignKeys = [ForeignKey(
        entity = Topic::class,
        parentColumns = ["id"],
        childColumns = ["topicId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val topicId: Int,
    val sessionDate: String,
    val duration: Int = 25
)