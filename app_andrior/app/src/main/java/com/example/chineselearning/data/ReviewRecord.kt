package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "review_records")
data class ReviewRecord(
    @PrimaryKey val characterId: Int,
    @ColumnInfo(name = "last_review_time") val lastReviewTime: Long,
    @ColumnInfo(name = "review_count") val reviewCount: Int,
    @ColumnInfo(name = "next_review_time") val nextReviewTime: Long  // 添加列名注解
)