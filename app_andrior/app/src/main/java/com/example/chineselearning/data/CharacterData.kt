package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "characters")
data class CharacterData(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "character")
    val character: String,    // 汉字

    @ColumnInfo(name = "pinyin")
    val pinyin: String,      // 拼音

    @ColumnInfo(name = "meaning")
    val meaning: String,     // 含义

    @ColumnInfo(name = "strokes")
    val strokes: Int,        // 笔画数

    @ColumnInfo(name = "examples")
    val examples: String,    // 例句

    @ColumnInfo(name = "level")
    val level: Int = 1,  // 添加 level 字段，默认为 1

    @ColumnInfo(name = "last_review_time")
    var lastReviewTime: Long = 0L,

    @ColumnInfo(name = "next_review_time")
    var nextReviewTime: Long = 0L,

    @ColumnInfo(name = "review_count")
    var reviewCount: Int = 0,

    @ColumnInfo(name = "mastery_level")
    var masteryLevel: Int = 0,

    @ColumnInfo(name = "is_learned")
    var isLearned: Boolean = false,  // 是否已学习

    @ColumnInfo(name = "learned_time")
    var learnedTime: Long? = null    // 首次学习时间
)