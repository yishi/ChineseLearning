package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey
    val characterId: Int,

    @ColumnInfo(name = "learned_time")
    val learnedTime: Long,

    @ColumnInfo(name = "is_learned")
    val isLearned: Boolean
)