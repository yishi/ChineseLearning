package com.example.chineselearning.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo

@Dao
interface CharacterDao {

    // ===== 基础数据操作 =====

    // 获取所有汉字数据，用于初始化
    // 作用：从数据库中获取所有汉字数据
    // 使用场景：检查数据库是否为空（需要初始化）,获取所有可学习的汉字,用于统计和显示总体学习进度
    @Query("SELECT * FROM characters")
    suspend fun getAllCharacters(): List<CharacterData>

    // 批量插入汉字，用于初始化
    // 作用：批量插入预设的汉字数据
    // 使用场景：在 CharacterRepository 的 initializeReviewRecordsIfNeeded() 中：
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterData>)

    // ===== 学习流程相关 =====
    // 1. 学习进度管理

    // 获取用户学习进度，用于UI显示
    @Query("SELECT * FROM learning_progress WHERE userId = :userId")
    fun getLearningProgress(userId: Int): LiveData<LearningProgress>

    // 更新学习进度
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLearningProgress(progress: LearningProgress)

    // 同步获取最新学习进度
    @Query("SELECT * FROM learning_progress WHERE userId = :userId ORDER BY lastUpdateTime DESC LIMIT 1")
    suspend fun getLearningProgressSync(userId: Int): LearningProgress?

    // 2. 学习记录管理

    // 创建学习记录，标记字符为已学习
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningRecord(record: LearningRecord)

    // 同步版本，suspend 版本返回 List，用于一次性获取数据
    // 获取所有已学习的字符，完整数据（包含汉字、拼音、释义等）用于UI显示
    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN learning_records lr ON c.id = lr.character_id
        WHERE lr.user_id = :userId AND lr.is_learned = 1
    """)
    suspend fun getAllLearnedCharacters(userId: Int): List<CharacterData>

    // LiveData版本，返回 LiveData，用于观察数据变化
    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN learning_records lr ON c.id = lr.character_id
        WHERE lr.user_id = :userId AND lr.is_learned = 1
     """)
    fun getAllLearnedCharactersLive(userId: Int): LiveData<List<CharacterData>>

    //用于获取学习记录详情
    @Query("SELECT * FROM learning_records WHERE user_id = :userId")
    suspend fun getAllLearningRecords(userId: Int): List<LearningRecord>

    // 只获取ID， 主要用于逻辑判断和过滤
    @Query("SELECT character_id FROM learning_records WHERE user_id = :userId AND is_learned = 1")
    suspend fun getLearnedCharacterIds(userId: Int): List<Int>

    @Query("""
        SELECT * FROM learning_records 
        WHERE character_id = :characterId AND user_id = :userId
    """)
    suspend fun getLearningRecordForCharacter(characterId: Int, userId: Int): LearningRecord?


    // ===== 复习流程相关 =====
    // 1. 复习记录基础操作
    // 创建/更新复习记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewRecord(record: ReviewRecord)

    // 获取特定字符的复习记录
    @Query("SELECT * FROM review_records WHERE characterId = :characterId AND userId = :userId")
    suspend fun getReviewRecordForCharacter(characterId: Int, userId: Int): ReviewRecord?

    // 获取用户所有复习记录
    // 修改为按用户ID获取复习记录
    @Query("""
        SELECT * FROM review_records 
        WHERE userId = :userId
    """)
    suspend fun getAllReviewRecords(userId: Int): List<ReviewRecord>

    // 2. 复习查询

    // 获取需要复习的字符，包含了字符的所有信息（汉字、拼音、释义等）
    // 完整的 CharacterData 对象列表
    // 检查学习记录（必须是已学习的）
    // 按复习时间排序
    // 使用场景：在 ReviewActivity 中显示待复习的汉字
    // WHERE rr.next_review_time <=  (:currentTime + 1000)
/*    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.next_review_time <=  :currentTime
        AND rr.userId = :userId
        AND EXISTS (
            SELECT 1 FROM learning_records lr 
            WHERE lr.character_id = c.id 
            AND lr.user_id = :userId 
            AND lr.is_learned = 1)
        ORDER BY rr.next_review_time ASC
    """)
    suspend fun getCharactersForReview(currentTime: Long, userId: Long): List<CharacterData>*/

    @Query("""
        SELECT 
            rr.characterId as characterId,
            rr.next_review_time as nextReviewTime,
            lr.is_learned as isLearned,
            :currentTime as currentTime,
            CASE WHEN rr.next_review_time <= :currentTime THEN 1 ELSE 0 END as isDue
        FROM review_records rr
        INNER JOIN learning_records lr 
            ON rr.characterId = lr.character_id
            AND rr.userId = :userId 
            AND lr.user_id = :userId
        WHERE rr.characterId = 1
    """)
    suspend fun debugReviewRecordDetails(currentTime: Long, userId: Int): List<ReviewRecordDebug>

    data class ReviewRecordDebug(
        @ColumnInfo(name = "characterId") val characterId: Int,
        @ColumnInfo(name = "nextReviewTime") val nextReviewTime: Long,
        @ColumnInfo(name = "isLearned") val isLearned: Boolean,
        @ColumnInfo(name = "currentTime") val currentTime: Long,
        @ColumnInfo(name = "isDue") val isDue: Boolean
    )
//  1/2
    @Query("""
    -- 先获取需要复习的记录
    SELECT rr.characterId 
    FROM review_records rr
    INNER JOIN learning_records lr 
        ON rr.characterId = lr.character_id
        AND lr.user_id = :userId
        AND lr.is_learned = 1
    WHERE rr.userId = :userId 
    AND rr.next_review_time <= :currentTime
    ORDER BY rr.next_review_time ASC
""")
    suspend fun getCharacterIdsForReview(currentTime: Long, userId: Int): List<Int>

    // 2/2
    // 然后根据ID获取字符详情
    @Query("SELECT * FROM characters WHERE id IN (:ids)")
    suspend fun getCharactersByIds(ids: List<Int>): List<CharacterData>

    // 3. 掌握度相关
    // 获取已掌握的字符
    @Query("""

        SELECT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.userId = :userId AND rr.review_count >= 3
        AND rr.next_review_time > (rr.last_review_time + 43200000)  -- 12小时以上

    """)
    suspend fun getMasteredCharacters(userId: Int): List<CharacterData>

    // 获取已掌握字符的实时更新
    @Query("""
        SELECT DISTINCT c.* FROM characters c
        INNER JOIN review_records rr ON c.id = rr.characterId
        WHERE rr.userId = :userId 
        AND rr.review_count >= 3 
        AND rr.next_review_time > (rr.last_review_time + 43200000)
    """)
    fun getMasteredCharactersLive(userId: Int): LiveData<List<CharacterData>>


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



    // 已废弃，使用 Repository 中的方法替代
    @Query("SELECT * FROM characters WHERE level = :level")
    suspend fun getCharactersByLevel(level: Int): List<CharacterData>

}

// ===== 已废弃或重复的函数 =====

// @Insert(onConflict = OnConflictStrategy.REPLACE)
// suspend fun insertCharacter(character: CharacterData)

// 仅用于调试
//  @Query("SELECT * FROM characters")
//  suspend fun debugGetAllCharacters(): List<CharacterData>

// 仅用于调试
//   @Query("SELECT * FROM review_records")
//   suspend fun debugGetAllReviewRecords(): List<ReviewRecord>

//  @Query("SELECT * FROM characters WHERE id IN (:ids)")
//  suspend fun getCharactersByIds(ids: List<Int>): List<CharacterData>

// 已废弃，字符数据应该是只读的
// @Update
// suspend fun updateCharacter(character: CharacterData)

// 已废弃，不应该批量初始化复习记录
// @Insert(onConflict = OnConflictStrategy.REPLACE)
//   suspend fun insertInitialReviewRecords(records: List<ReviewRecord>)  // 插入单个汉字，很少使用

// 已废弃，未使用
// @Query("SELECT * FROM characters WHERE id = :characterId")
// suspend fun getCharacterById(characterId: Int): CharacterData?

//  @Query("""
//      SELECT COUNT(DISTINCT characterId)
//      FROM review_records
//      WHERE userId = :userId
//      AND next_review_time <= :currentTime
//  """)
//   suspend fun getDistinctCharactersForReview(userId: Int, currentTime: Long): Int

// 获取待复习字符的数量
//  @Query("""
//      SELECT COUNT(*) FROM review_records rr
//      WHERE rr.userId = :userId
//      AND rr.next_review_time <=  :currentTime
//      AND EXISTS (
//          SELECT 1 FROM learning_records lr
//          WHERE lr.character_id = rr.characterId
//          AND lr.user_id = :userId
//          AND lr.is_learned = 1
//      )
//   """)
// suspend fun getReviewRecordsCount(userId: Int, currentTime: Long): Int
