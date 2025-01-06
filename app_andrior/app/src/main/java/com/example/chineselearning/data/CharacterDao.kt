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
        AND rr.userId = :userId
        AND EXISTS (
            SELECT 1 FROM learning_records lr 
            WHERE lr.character_id = c.id 
            AND lr.user_id = :userId 
            AND lr.is_learned = 1
        )
        ORDER BY rr.next_review_time ASC
    """)
    suspend fun getCharactersForReview(currentTime: Long, userId: Int): List<CharacterData>



    @Query("SELECT * FROM review_records WHERE characterId = :characterId")
    suspend fun getReviewRecordForCharacter(characterId: Int): ReviewRecord?

    // 获取已学习的字符
    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN learning_records lr ON c.id = lr.character_id
        WHERE lr.user_id = :userId AND lr.is_learned = 1
    """)
    suspend fun getAllLearnedCharacters(userId: Int): List<CharacterData>

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

    @Query("""
        SELECT COUNT(*) FROM review_records rr
        WHERE rr.userId = :userId 
        AND rr.next_review_time <= :currentTime
        AND EXISTS (
            SELECT 1 FROM learning_records lr 
            WHERE lr.character_id = rr.characterId 
            AND lr.user_id = :userId 
            AND lr.is_learned = 1
        )
    """)
    suspend fun getReviewRecordsCount(userId: Int, currentTime: Long): Int


    @Query("""

        SELECT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.userId = :userId AND rr.review_count >= 3
        AND rr.next_review_time > (rr.last_review_time + 43200000)  -- 12小时以上

    """)
    suspend fun getMasteredCharacters(userId: Int): List<CharacterData>

    // 获取学习进度
    @Query("SELECT * FROM learning_progress WHERE userId = :userId ORDER BY lastUpdateTime DESC LIMIT 1")
    suspend fun getLearningProgressSync(userId: Int): LearningProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterData>)

    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN learning_records lr ON c.id = lr.character_id
        WHERE lr.user_id = :userId AND lr.is_learned = 1
    """)
    fun getAllLearnedCharactersLive(userId: Int): LiveData<List<CharacterData>>

    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.userId = :userId 
        AND rr.review_count >= 3 
        AND rr.next_review_time > (rr.last_review_time + 43200000)
    """)
    fun getMasteredCharactersLive(userId: Int): LiveData<List<CharacterData>>

    @Query("""
        SELECT * FROM learning_records 
        WHERE character_id = :characterId AND user_id = :userId
    """)
    suspend fun getLearningRecordForCharacter(characterId: Int, userId: Int): LearningRecord?

    // 修改为按用户ID获取复习记录
    @Query("""
        SELECT * FROM review_records 
        WHERE userId = :userId
    """)
    suspend fun getAllReviewRecords(userId: Int): List<ReviewRecord>

    @Query("""
    SELECT c.*, 
           CASE 
               WHEN c.level = 1 THEN ((c.id - 1) / 10) + 1 
               ELSE c.level 
           END AS calculated_level 
    FROM characters c
    LEFT JOIN learning_records lr ON 
        lr.character_id = c.id AND 
        lr.user_id = :userId AND 
        lr.is_learned = 1
    WHERE lr.id IS NULL
    AND CASE 
        WHEN c.level = 1 THEN ((c.id - 1) / 10) + 1 
        ELSE c.level 
    END >= :level
    ORDER BY calculated_level ASC, c.id ASC
    LIMIT 10
""")
    suspend fun getCharactersForLevel(level: Int, userId: Int): List<CharacterData>

    @Query("""
        SELECT c.* FROM characters c 
        WHERE c.id NOT IN (
            SELECT lr.character_id 
            FROM learning_records lr 
            WHERE lr.user_id = :userId AND lr.is_learned = 1
        )
        ORDER BY c.level ASC, c.id ASC 
        LIMIT :count
    """)
    suspend fun getUnlearnedCharacters(userId: Int, count: Int): List<CharacterData>

    @Query("SELECT character_id FROM learning_records WHERE user_id = :userId AND is_learned = 1")
    suspend fun getLearnedCharacterIds(userId: Int): List<Int>

    @Query("""
    SELECT c.* FROM characters c
    LEFT JOIN learning_records lr ON 
        lr.character_id = c.id AND 
        lr.user_id = :userId AND 
        lr.is_learned = 1
    WHERE lr.id IS NULL
    AND c.id > (
        SELECT COALESCE(MAX(character_id), 0) 
        FROM learning_records 
        WHERE user_id = :userId AND is_learned = 1
    )
    ORDER BY c.id ASC
    LIMIT :count
""")
    suspend fun getNextCharacters(userId: Int, count: Int = 10): List<CharacterData>

    @Query("""
        SELECT COALESCE(MAX(character_id), 0)
        FROM learning_records
        WHERE user_id = :userId AND is_learned = 1
    """)
    suspend fun getLastLearnedCharacterId(userId: Int): Int
}