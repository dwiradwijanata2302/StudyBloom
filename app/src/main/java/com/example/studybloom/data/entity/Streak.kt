package com.example.studybloom.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak")
data class Streak(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String, // yyyy-MM-dd
    val streakValue: Int,
    val sessionsCount: Int,
    val hasBonus: Boolean = false
)