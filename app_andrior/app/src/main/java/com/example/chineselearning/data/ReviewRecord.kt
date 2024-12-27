package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "review_records")
data class ReviewRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val characterId: Int,
    val userId: Int = 0,
    @ColumnInfo(name = "review_count")
    var reviewCount: Int = 0,
    @ColumnInfo(name = "last_review_time")
    var lastReviewTime: Long = 0,
    @ColumnInfo(name = "next_review_time")
    var nextReviewTime: Long = 0,
    @ColumnInfo(name = "mastery_level")
    var masteryLevel: Int = 0
)


