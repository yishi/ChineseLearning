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
    suspend fun getAllCharacters(): List<CharacterData>

    @Query("SELECT COUNT(*) FROM review_records")
    suspend fun getReviewRecordsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInitialReviewRecords(records: List<ReviewRecord>)

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

    @Query("""
        SELECT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.next_review_time <= :currentTime
        ORDER BY rr.next_review_time ASC
    """)
    suspend fun getCharactersForReview(currentTime: Long): List<CharacterData>

    @Query("SELECT * FROM review_records")
    suspend fun getAllReviewRecords(): List<ReviewRecord>

    @Query("SELECT * FROM review_records WHERE characterId = :characterId")
    suspend fun getReviewRecordForCharacter(characterId: Int): ReviewRecord?

    @Query("""
        SELECT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
    """)
    suspend fun getAllLearnedCharacters(): List<CharacterData>

    @Query("SELECT * FROM characters WHERE id IN (:ids)")
    suspend fun getCharactersByIds(ids: List<Int>): List<CharacterData>

    @Query("SELECT * FROM characters WHERE level = :level")
    suspend fun getCharactersByLevel(level: Int): List<CharacterData>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: Int): CharacterData?

    @Query("SELECT * FROM characters")
    suspend fun debugGetAllCharacters(): List<CharacterData>

    @Query("SELECT * FROM review_records")
    suspend fun debugGetAllReviewRecords(): List<ReviewRecord>
}