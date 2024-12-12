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

    @ColumnInfo(name = "last_review_time")
    val lastReviewTime: Long,  // 上次复习时间

    @ColumnInfo(name = "next_review_time")
    val nextReviewTime: Long,  // 下次复习时间

    @ColumnInfo(name = "review_count")
    val reviewCount: Int,      // 复习次数

    @ColumnInfo(name = "mastery_level")
    val masteryLevel: Int,     // 掌握程度

    @ColumnInfo(name = "is_learned")
    var isLearned: Boolean = false,  // 是否已学习

    @ColumnInfo(name = "learned_time")
    var learnedTime: Long? = null    // 首次学习时间
)