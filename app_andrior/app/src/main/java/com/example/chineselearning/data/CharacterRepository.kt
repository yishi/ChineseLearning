package com.example.chineselearning.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.chineselearning.utils.ReviewIntervalCalculator

class CharacterRepository(private val dao: CharacterDao) {

    private var currentUserId: Int = -1

    fun setCurrentUser(userId: Int) {
        currentUserId = userId
        Log.d("CharacterRepository", "Set current user ID to: $userId")
    }

    fun getLearningProgress(userId: Int): LiveData<LearningProgress> {
        return dao.getLearningProgress(userId)
    }

    suspend fun saveLearningProgress(progress: LearningProgress) {
        dao.insertOrUpdateLearningProgress(progress)
    }

    suspend fun markCharacterAsLearned(characterId: Int) {
        if (currentUserId == -1) {
            Log.e("CharacterRepository", "Current user ID not set!")
            return
        }

        val currentTime = System.currentTimeMillis()
        Log.d("CharacterRepository", "Marking character $characterId as learned for user $currentUserId")

        // 检查是否已经学习过
        val existingRecord = dao.getLearningRecordForCharacter(characterId, currentUserId)
        if (existingRecord == null) {
            // 创建学习记录
            dao.insertLearningRecord(
                LearningRecord(
                    characterId = characterId,
                    userId = currentUserId,
                    learnedTime = currentTime,
                    isLearned = true
                )
            )
            Log.d("CharacterRepository", "Created learning record for character $characterId")

        // 创建复习记录，确保立即可复习
        val reviewRecord = ReviewRecord(
                characterId = characterId,
                userId = currentUserId,
                reviewCount = 0,
                lastReviewTime = currentTime,
                nextReviewTime = currentTime, // 当前时间，确保立即可复习
                masteryLevel = 0
            )
        dao.insertReviewRecord(reviewRecord)
            Log.d("CharacterRepository",
                "Created new learning and review records for character $characterId"
            )
        }

        // 更新学习进度
        val progress = LearningProgress(
            userId = currentUserId,
            lastCharacterId = characterId,
            lastUpdateTime = currentTime
        )
        dao.insertOrUpdateLearningProgress(progress)
    }

    // 在现有的 CharacterRepository 类中添加以下方法：
    suspend fun getCharactersForReview(): List<CharacterData> {
        if (currentUserId == -1) {
            Log.e("CharacterRepository", "Current user ID not set!")
            return emptyList()
        }

        val currentTime = System.currentTimeMillis()
        // 添加调试日志
        val reviewCharacters = dao.getCharactersForReview(currentTime, currentUserId)
        // 详细日志
        Log.d("CharacterRepository", "Checking review status for user $currentUserId:")
        Log.d("CharacterRepository", "Current time: $currentTime")
        Log.d("CharacterRepository", "Found ${reviewCharacters.size} characters for review")

        // 调试信息
        val learnedCharacters = dao.getAllLearnedCharacters(currentUserId)
        Log.d("CharacterRepository", "Learned characters: ${learnedCharacters.map { it.id }}")
        Log.d("CharacterRepository", "Total learned characters: ${learnedCharacters.size}")

        val reviewRecords = dao.getAllReviewRecords(currentUserId)
        Log.d("CharacterRepository", "Total review records for user $currentUserId: ${reviewRecords.size}")

        reviewRecords.forEach { record ->
            Log.d("CharacterRepository",
                "Review record - CharID: ${record.characterId}, " +
                        "NextReview: ${record.nextReviewTime}, " +
                        "Due: ${record.nextReviewTime <= currentTime}"
            )
        }

        return reviewCharacters
    }

    suspend fun getAllReviewRecords(): List<ReviewRecord> {
        if (currentUserId == -1) {
            throw IllegalStateException("User ID not set. Call setCurrentUser first.")
        }
        return dao.getAllReviewRecords(currentUserId)
    }

    suspend fun updateReviewStatus(characterId: Int, remembered: Boolean) {
        val reviewRecord = dao.getReviewRecordForCharacter(characterId)
        reviewRecord?.let {
            val currentTime = System.currentTimeMillis()
            val newReviewCount = it.reviewCount + 1
            val newNextReviewTime = if (remembered) {
                calculateNextReviewTime(newReviewCount)
            } else {
                // 如果没记住，1小时后再复习
                currentTime + 3600000
            }

            dao.insertReviewRecord(
                ReviewRecord(
                    id = it.id,
                    characterId = characterId,
                    userId = currentUserId,
                    lastReviewTime = currentTime,
                    reviewCount = newReviewCount,
                    nextReviewTime = newNextReviewTime,
                    masteryLevel = if (remembered) it.masteryLevel + 1 else it.masteryLevel
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
            1 -> currentTime // 立即复习
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
        // 只初始化汉字数据，不初始化复习记录
        if (dao.getAllCharacters().isEmpty()) {
            // 从 CharacterDataList 获取预设的汉字数据并插入数据库
            val characters = CharacterDataList.getCharacterList()
            dao.insertCharacters(characters)
            Log.d("CharacterRepository", "Initialized ${characters.size} characters")
        }
    }

    suspend fun getAllLearnedCharacters() = dao.getAllLearnedCharacters(currentUserId)

    suspend fun getCharactersByLevel(level: Int): List<CharacterData> {
        Log.d("CharacterRepository", "Getting characters for level: $level")
        // 获取用户的学习进度
        val progress = dao.getLearningProgressSync(currentUserId)
        val lastLearnedId = progress?.lastCharacterId ?: 0
        // 获取所有汉字
        val allCharacters = dao.getAllCharacters()
        if (allCharacters.isEmpty()) {
            Log.d("CharacterRepository", "No characters found in database")
            return emptyList()
        }

        // 计算当前级别的起始和结束索引
        val charactersPerLevel = 10  // 每级10个汉字
        val startIndex = (level - 1) * charactersPerLevel

        // 如果起始索引超出了总汉字数，返回空列表
        if (startIndex >= allCharacters.size) {
            Log.d("CharacterRepository", "Level $level exceeds available characters")
            return emptyList()
        }

        // 获取这一级别的汉字
        val endIndex = minOf(startIndex + charactersPerLevel, allCharacters.size)
        val characters = allCharacters.subList(startIndex, endIndex)
        Log.d(
            "CharacterRepository",
            "Found ${characters.size} characters for level $level, starting from ID: ${characters.firstOrNull()?.id}"
        )
        return characters
    }

    suspend fun getAllCharacters(): List<CharacterData> {
        return dao.getAllCharacters()
    }

    suspend fun getMasteredCharacters() = dao.getMasteredCharacters(currentUserId)

    fun getAllLearnedCharactersLive(userId: Int): LiveData<List<CharacterData>> {
        return dao.getAllLearnedCharactersLive(userId)
    }

    fun getMasteredCharactersLive(userId: Int): LiveData<List<CharacterData>> {
        return dao.getMasteredCharactersLive(userId)
    }

    suspend fun getReviewCount(): Int {
        if (currentUserId == -1) {
            throw IllegalStateException("User ID not set. Call setCurrentUser first.")
        }
        val currentTime = System.currentTimeMillis()
        return dao.getReviewRecordsCount(currentUserId, currentTime)
    }

    suspend fun getCurrentLevel(userId: Int): Int {
        val progress = dao.getLearningProgressSync(userId)
        // 从学习记录中计算当前级别
        val learnedCharacters = dao.getAllLearnedCharacters(userId)
        val charactersPerLevel = 10
        return if (learnedCharacters.isEmpty()) 1 else {
            (learnedCharacters.size / charactersPerLevel) + 1
        }
    }

    suspend fun exportUserData(userId: Int): UserData {
        return UserData(
            learningProgress = dao.getLearningProgressSync(userId),
            learnedCharacters = dao.getAllLearnedCharacters(userId),
            reviewRecords = dao.getAllReviewRecords(userId)
        )
    }

    suspend fun importUserData(userId: Int, userData: UserData) {
        userData.learningProgress?.let { progress ->
            dao.insertOrUpdateLearningProgress(progress)
        }
        userData.learnedCharacters.forEach { character ->
            markCharacterAsLearned(character.id)
        }
        userData.reviewRecords.forEach { record ->
            dao.insertReviewRecord(record.copy(userId = userId))
        }
    }

    data class UserData(
        val learningProgress: LearningProgress?,
        val learnedCharacters: List<CharacterData>,
        val reviewRecords: List<ReviewRecord>
    )
}