package com.example.chineselearning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)