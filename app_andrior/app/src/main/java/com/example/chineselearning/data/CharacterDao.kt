package com.example.chineselearning.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData
import androidx.room.Update

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterData>>

    @Query("""
        SELECT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.next_review_time <= :currentTime
        ORDER BY rr.next_review_time ASC
    """)
    fun getCharactersForReview(currentTime: Long): LiveData<List<CharacterData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterData)

    @Update
    suspend fun updateCharacter(character: CharacterData)

    @Query("SELECT * FROM learning_progress WHERE userId = :userId")
    fun getLearningProgress(userId: Int): LiveData<LearningProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLearningProgress(progress: LearningProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningRecord(record: LearningRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewRecord(record: ReviewRecord)
}