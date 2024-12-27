package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_progress")
data class LearningProgress(
    @PrimaryKey
    val userId: Int,
    val lastCharacterId: Int,
    val lastUpdateTime: Long,
    val currentLevel: Int = 1
)