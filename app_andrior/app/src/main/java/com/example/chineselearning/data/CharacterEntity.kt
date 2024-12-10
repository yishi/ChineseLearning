package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val character: String,
    val pinyin: String,
    val meaning: String,
    val strokes: Int,
    val examples: String,
    val lastReviewTime: Long,
    val nextReviewTime: Long,
    val reviewCount: Int,
    val masteryLevel: Int
)