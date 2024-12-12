package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey val characterId: Int,
    val learnedTime: Long,
    val isLearned: Boolean
)