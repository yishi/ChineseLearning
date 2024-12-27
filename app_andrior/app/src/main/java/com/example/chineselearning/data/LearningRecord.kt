package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "character_id")
    val characterId: Int,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "learned_time")
    val learnedTime: Long,

    @ColumnInfo(name = "is_learned")
    val isLearned: Boolean
)