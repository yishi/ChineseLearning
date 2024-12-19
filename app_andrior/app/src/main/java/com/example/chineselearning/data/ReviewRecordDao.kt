package com.example.chineselearning.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.lifecycle.LiveData

@Dao
interface ReviewRecordDao {
    @Query("SELECT * FROM review_records")
    fun getAllReviewRecords(): LiveData<List<ReviewRecord>>

    @Query("SELECT * FROM review_records WHERE characterId = :characterId")
    fun getReviewRecordForCharacter(characterId: Int): LiveData<ReviewRecord?>

    @Query("SELECT * FROM review_records WHERE next_review_time <= :currentTime")
    fun getReviewsDue(currentTime: Long): LiveData<List<ReviewRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reviewRecord: ReviewRecord)

    @Update
    suspend fun update(reviewRecord: ReviewRecord)
}