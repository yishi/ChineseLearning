package com.example.chineselearning.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.chineselearning.utils.ReviewIntervalCalculator

class CharacterRepository(internal val dao: CharacterDao) {
    fun getLearningProgress(userId: Int): LiveData<LearningProgress> {
        return dao.getLearningProgress(userId)
    }

    suspend fun saveLearningProgress(progress: LearningProgress) {
        dao.insertOrUpdateLearningProgress(progress)
    }

    suspend fun markCharacterAsLearned(characterId: Int) {
        val learnedTime = System.currentTimeMillis()
        // 创建学习记录
        dao.insertLearningRecord(
            LearningRecord(
                characterId = characterId,
                learnedTime = learnedTime,
                isLearned = true
            )
        )
        // 同时创建首次复习记录
        dao.insertReviewRecord(
            ReviewRecord(
                characterId = characterId,
                lastReviewTime = learnedTime,
                reviewCount = 0,
                nextReviewTime = ReviewIntervalCalculator.calculateNextReviewTime(0)
            )
        )
    }

    // 在现有的 CharacterRepository 类中添加以下方法：
    suspend fun getCharactersForReview(): List<CharacterData> {
        val currentTime = System.currentTimeMillis()
        Log.d("CharacterRepository", "Getting characters for review at time: $currentTime")
        
        // 添加调试信息
        val allReviewRecords = dao.getAllReviewRecords()
        Log.d("CharacterRepository", "Debug - Total review records before query: ${allReviewRecords.size}")
        val dueRecords = allReviewRecords.filter { it.nextReviewTime <= currentTime }
        Log.d("CharacterRepository", "Debug - Records due for review: ${dueRecords.size}")
        
        val characters = dao.getCharactersForReview(currentTime)
        Log.d("CharacterRepository", "Found ${characters.size} characters for review")
        return characters
    }

    suspend fun getAllReviewRecords(): List<ReviewRecord> {
        return dao.getAllReviewRecords()
    }

    suspend fun updateReviewStatus(characterId: Int, remembered: Boolean) {
        val reviewRecord = dao.getReviewRecordForCharacter(characterId)
        reviewRecord?.let {
            val newReviewCount = it.reviewCount + 1
            val currentTime = System.currentTimeMillis()
            val newNextReviewTime = if (remembered) {
                calculateNextReviewTime(newReviewCount)
            } else {
                // 如果没记住，1小时后再复习
                currentTime + 3600000
            }

            dao.insertReviewRecord(
                ReviewRecord(
                    characterId = characterId,
                    lastReviewTime = currentTime,
                    reviewCount = newReviewCount,
                    nextReviewTime = newNextReviewTime
                )
            )
            
            // 添加日志
            Log.d("CharacterRepository", 
                "Updated review status - CharID: $characterId, " +
                "Remembered: $remembered, " +
                "New ReviewCount: $newReviewCount, " +
                "Next Review in: ${(newNextReviewTime - currentTime) / 3600000}h"
            )
        }
    }

    private fun calculateNextReviewTime(reviewCount: Int): Long {
        val currentTime = System.currentTimeMillis()
        return when (reviewCount) {
            1 -> currentTime + 5 * 60 * 1000      // 5分钟后
            2 -> currentTime + 30 * 60 * 1000     // 30分钟后
            3 -> currentTime + 12 * 3600 * 1000   // 12小时后
            4 -> currentTime + 24 * 3600 * 1000   // 1天后
            5 -> currentTime + 2 * 24 * 3600 * 1000  // 2天后
            6 -> currentTime + 4 * 24 * 3600 * 1000  // 4天后
            7 -> currentTime + 7 * 24 * 3600 * 1000  // 7天后
            else -> currentTime + 15 * 24 * 3600 * 1000  // 15天后
        }
    }

    suspend fun initializeReviewRecordsIfNeeded() {
        try {
            // 首先检查字符数据
            val allCharacters = dao.getAllCharacters()
            if (allCharacters.isEmpty()) {
                Log.d("CharacterRepository", "Initializing characters database")
                // 从 CharacterDataList 获取预定义的字符数据
                val characterList = CharacterDataList.getCharacterList()
                characterList.forEach { character ->
                    dao.insertCharacter(character)
                }
                Log.d("CharacterRepository", "Initialized ${characterList.size} characters")
            }

            // 检查复习记录
            val count = dao.getReviewRecordsCount()
            Log.d("CharacterRepository", "Current review records count: $count")

            if (count == 0) {
                val characters = dao.getAllCharacters()
                Log.d("CharacterRepository", "Found ${characters.size} characters to initialize review records")

                if (characters.isNotEmpty()) {
                    val currentTime = System.currentTimeMillis()
                    val reviewRecords = characters.map { character ->
                        ReviewRecord(
                            characterId = character.id,
                            lastReviewTime = currentTime,
                            reviewCount = 0,
                            nextReviewTime = currentTime
                        )
                    }
                    dao.insertInitialReviewRecords(reviewRecords)
                    Log.d("CharacterRepository", "Initialized ${reviewRecords.size} review records")
                }
            }

            // 打印调试信息
            val debugCharacters = dao.getAllCharacters()
            val debugReviewRecords = dao.getAllReviewRecords()
            Log.d("CharacterRepository", "After initialization - Characters: ${debugCharacters.size}, Review Records: ${debugReviewRecords.size}")
        } catch (e: Exception) {
            Log.e("CharacterRepository", "Error initializing database", e)
            e.printStackTrace()
        }
    }

    suspend fun getAllLearnedCharacters(): List<CharacterData> {
        Log.d("CharacterRepository", "Getting all learned characters")
        
        // 添加调试信息
        val allCharacters = dao.getAllCharacters()
        val allReviewRecords = dao.getAllReviewRecords()
        Log.d("CharacterRepository", "Debug - Total characters: ${allCharacters.size}")
        Log.d("CharacterRepository", "Debug - Total review records: ${allReviewRecords.size}")
        
        val characters = dao.getAllLearnedCharacters()
        Log.d("CharacterRepository", "Found ${characters.size} learned characters")
        return characters
    }

    suspend fun getCharactersByLevel(level: Int): List<CharacterData> {
        Log.d("CharacterRepository", "Getting characters for level: $level")
        val characters = dao.getCharactersByLevel(level)
        Log.d("CharacterRepository", "Found ${characters.size} characters for level $level")
        return characters
    }

    suspend fun getAllCharacters(): List<CharacterData> {
        return dao.getAllCharacters()
    }

    suspend fun getMasteredCharacters(): List<CharacterData> {
        val reviewRecords = dao.getAllReviewRecords()
        Log.d("CharacterRepository", "Total review records: ${reviewRecords.size}")
        
        val masteredCharacterIds = reviewRecords
            .filter { record -> 
                // 修改掌握标准：
                // 1. 复习次数至少3次
                // 2. 最后一次复习时间��离下一次复习时间超过12小时
                // (说明上次复习是"记住了")
                record.reviewCount >= 3 && 
                (record.nextReviewTime - record.lastReviewTime > 12 * 3600 * 1000)
            }
            .map { it.characterId }
        
        Log.d("CharacterRepository", "Mastered character count: ${masteredCharacterIds.size}")
        
        return dao.getCharactersByIds(masteredCharacterIds)
    }
}