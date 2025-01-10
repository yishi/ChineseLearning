package com.example.chineselearning.data

import android.util.Log
import androidx.lifecycle.LiveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // 1. 创建复习记录
    suspend fun markCharacterAsLearned(characterId: Int) {
        if (currentUserId == -1) {
            Log.e("CharacterRepository", "Current user ID not set!")
            return
        }

        val currentTime = System.currentTimeMillis()
        Log.d("CharacterRepository", "Marking character $characterId as learned for user $currentUserId")

        // 更新学习记录
        val learningRecord = LearningRecord(
            characterId = characterId,
            userId = currentUserId,
            learnedTime = currentTime,
            isLearned = true  // 直接标记为已学习
        )
        dao.insertLearningRecord(learningRecord)

        Log.d("CharacterRepository", """
        开始创建复习记录:
        - 字符ID: $characterId
        - 用户ID: $currentUserId
        - 当前时间: $currentTime
        - 时间格式化: ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(currentTime))}
    """.trimIndent())

        // 创建复习记录，确保立即可复习
        val reviewRecord = ReviewRecord(
            characterId = characterId,
            userId = currentUserId,
            reviewCount = 0,
            lastReviewTime = currentTime - 1000,
            nextReviewTime = currentTime - 500, // 当前时间，确保立即可复习
            masteryLevel = 0
        )

        Log.d("CharacterRepository", """
        复习记录详情:
        - ID: ${reviewRecord.id}
        - 字符ID: ${reviewRecord.characterId}
        - 用户ID: ${reviewRecord.userId}
        - 上次复习: ${reviewRecord.lastReviewTime}
        - 下次复习: ${reviewRecord.nextReviewTime}
        - 时间差(last): ${currentTime - reviewRecord.lastReviewTime}ms
        - 时间差(next): ${currentTime - reviewRecord.nextReviewTime}ms
        - 时间格式化: ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(currentTime))}
    """.trimIndent())

        dao.insertReviewRecord(reviewRecord)
        // 验证插入后的记录
        val savedRecord = dao.getReviewRecordForCharacter(characterId, currentUserId)
        Log.d("CharacterRepository", """
        保存后的复习记录:
        - ID: ${savedRecord?.id}
        - 字符ID: ${savedRecord?.characterId}
        - 用户ID: ${savedRecord?.userId}
        - 上次复习: ${savedRecord?.lastReviewTime}
        - 下次复习: ${savedRecord?.nextReviewTime}
        - 时间格式化: ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(currentTime))}
    """.trimIndent())

        Log.d(
            "CharacterRepository",
            "Created new learning and review records for character $characterId"
        )
        // 更新学习进度
        val progress = LearningProgress(
            userId = currentUserId,
            lastCharacterId = characterId,
            lastUpdateTime = currentTime
        )
        dao.insertOrUpdateLearningProgress(progress)
        Log.d("CharacterRepository", "Updated learning progress - LastCharID: $characterId")

    }

    // 2. 获取待复习字符
    /**
     * 获取需要复习的字符
     * 返回满足以下条件的字符：
     * 1. 已经学习过（learning_records 中存在记录且 is_learned = 1）
     * 2. 复习时间已到（next_review_time <= currentTime）
     */
    // MainActivity 可以继续使用无参数的 getCharactersForReview()
    suspend fun getCharactersForReview(): List<CharacterData> {
        val currentTime = System.currentTimeMillis()
        return getCharactersForReview(currentTime, currentUserId)
    }
    // Repository 内部使用带参数的版本进行实际查询
    private suspend fun getCharactersForReview(currentTime: Long, userId: Int): List<CharacterData> {
        // 1. 获取需要复习的字符ID
        val characterIds = dao.getCharacterIdsForReview(currentTime, userId)

        // 2. 如果没有需要复习的字符，直接返回空列表
        if (characterIds.isEmpty()) {
            return emptyList()
        }

        // 3. 获取这些字符的详细信息
        return dao.getCharactersByIds(characterIds)
    }



    /*suspend fun getCharactersForReview(): List<CharacterData> {
        if (currentUserId == -1) {
            throw IllegalStateException("User ID not set")
        }

        val currentTime = System.currentTimeMillis()

        // 添加调试查询
        val debugInfo = dao.debugReviewRecordDetails(currentTime, currentUserId)
        Log.d("CharacterRepository", """
            复习查询调试信息:
            - 当前时间: ${formatTime(currentTime)}
            - 用户ID: $currentUserId
            - 调试记录:
            ${debugInfo.joinToString("\n") { info ->
            """
                字符ID: ${info.characterId}
                - 下次复习时间: ${info.nextReviewTime?.let { formatTime(it) }}
                - 当前时间: ${formatTime(currentTime)}
                - 已学习: ${info.isLearned}
                - 已到期: ${info.isDue}
                """.trimIndent()
        }}
        """.trimIndent())

        // 1. 获取并分析学习记录
        val learningRecords = dao.getAllLearningRecords(currentUserId)
        //- 时间格式化: ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(currentTime))}
        val learningDetails = learningRecords.joinToString("\n") { record ->
            """
            字符ID: ${record.characterId}
            - 学习时间: ${formatTime(record.learnedTime)}
            - 是否已学习: ${record.isLearned}
            - 距今时间: ${(currentTime - record.learnedTime) / 1000}秒
            """.trimIndent()
        }

        Log.d("CharacterRepository", """
            学习记录详细分析:
            - 当前时间: ${formatTime(currentTime)}
            - 总记录数: ${learningRecords.size}
            - 记录详情:
            $learningDetails
        """.trimIndent())

        // 2. 获取并分析复习记录
        val reviewRecords = dao.getAllReviewRecords(currentUserId)
        val reviewDetails = reviewRecords.joinToString("\n") { record ->
            """
                字符ID: ${record.characterId}
                - 上次复习: ${formatTime(record.lastReviewTime)}
                - 下次复习: ${formatTime(record.nextReviewTime)}
                - 是否到期: ${record.nextReviewTime <= currentTime}
                - 复习次数: ${record.reviewCount}
                """.trimIndent()
        }

        Log.d("CharacterRepository", """
            复习记录详细分析:
            - 当前时间: ${formatTime(currentTime)}
            - 总记录数: ${reviewRecords.size}
            - 记录详情:
            $reviewDetails
        """.trimIndent())

        // 3. 执行复习查询
        val result = dao.getCharactersForReview(currentUserId.toLong(), currentTime)
        // 4. 分析查询结果
        val resultDetails = result.joinToString("\n") { char ->
                """
                ID: ${char.id}
                - 是否有学习记录: ${learningRecords.any { it.characterId == char.id }}
                - 学习状态: ${learningRecords.find { it.characterId == char.id }?.isLearned}
                - 是否有复习记录: ${reviewRecords.any { it.characterId == char.id }}
                - 复习时间: ${
                    reviewRecords.find { it.characterId == char.id }?.nextReviewTime?.let {
                        formatTime(
                            it
                        )
                    }
                }
                """.trimIndent()
            }
        val reviewTimeExpiredCount = result.count { char ->
            reviewRecords.any { it.characterId == char.id && it.nextReviewTime <= currentTime }
        }

        Log.d("CharacterRepository", """
            复习查询结果分析:
            - 当前时间: ${formatTime(currentTime)}
            - 找到字符数: ${result.size}
            - 字符列表:
            $resultDetails
            
            查询条件验证:
            - SQL参数: currentTime=${formatTime(currentTime)}, userId=$currentUserId
            - 学习记录匹配数: ${
            result.count { char ->
                learningRecords.any { it.characterId == char.id && it.isLearned }
            }
        }
            - 复习时间到期数: $reviewTimeExpiredCount   
    """.trimIndent())
        //return charactersForReview
        return result
    }*/

    /**
     * 格式化时间戳为可读格式
     * @param time 时间戳（毫秒）
     * @return 格式化的时间字符串（HH:mm:ss.SSS）
     */

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(time))
    }

  //  private fun formatTime(time: Long): String {
   //     return java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(time))
 //   }

    suspend fun getAllReviewRecords(): List<ReviewRecord> {
        if (currentUserId == -1) {
            throw IllegalStateException("User ID not set. Call setCurrentUser first.")
        }
        return dao.getAllReviewRecords(currentUserId)
    }

    suspend fun updateReviewStatus(characterId: Int, remembered: Boolean) {
        val reviewRecord = dao.getReviewRecordForCharacter(characterId, currentUserId)
        reviewRecord?.let {
            val currentTime = System.currentTimeMillis()
            val newReviewCount = it.reviewCount + 1  // 复习次数+1
            val newNextReviewTime = if (remembered) {
                calculateNextReviewTime(newReviewCount) // 记住了就按间隔计算
            } else {
                // 如果没记住，1小时后再复习
                currentTime + 3600000
            }

            dao.insertReviewRecord(
                ReviewRecord(
                    id = it.id,
                    characterId = characterId,
                    userId = currentUserId,
                    lastReviewTime = currentTime,    // 更新上次复习时间
                    reviewCount = newReviewCount,   // 更新复习次数
                    nextReviewTime = newNextReviewTime,  // 更新下次复习时间
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
            1 -> currentTime // 首次复习：立即复习
            2 -> currentTime + 30 * 60 * 1000     // 第二次：30分钟后
            3 -> currentTime + 12 * 3600 * 1000   // 第三次：12小时后
            4 -> currentTime + 24 * 3600 * 1000   // 第四次：1天后
            5 -> currentTime + 2 * 24 * 3600 * 1000  // 第五次：2天后
            6 -> currentTime + 4 * 24 * 3600 * 1000  // 第六次：4天后
            7 -> currentTime + 7 * 24 * 3600 * 1000  // 第七次：7天后
            else -> currentTime + 15 * 24 * 3600 * 1000  // 第八次：15天后
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

    // 同步版本，返回 List，用于一次性获取数据
    suspend fun getAllLearnedCharacters() = dao.getAllLearnedCharacters(currentUserId)

    // LiveData版本，返回 LiveData，用于观察数据变化，用在文件StatisticActivity.kt文件中
    fun getAllLearnedCharactersLive(userId: Int): LiveData<List<CharacterData>> {
        return dao.getAllLearnedCharactersLive(userId)
    }

    suspend fun getCharactersByLevel(level: Int): List<CharacterData> {
        Log.d("CharacterRepository", "Getting characters for level $level")

        // 获取用户的学习进度
        val progress = dao.getLearningProgressSync(currentUserId)
        Log.d("CharacterRepository", "Current learning progress - LastCharID: ${progress?.lastCharacterId}")

        // 获取该级别的所有字符
        val characters = dao.getCharactersByLevel(level)

        // 如果有学习进度，从上次学习的字符之后开始
        return if (progress != null) {
            // 获取该用户最后学习的字符
            val lastLearned = dao.getLearningRecordForCharacter(progress.lastCharacterId, currentUserId)
            if (lastLearned?.isLearned == true) {
                val startIndex = characters.indexOfFirst { it.id > progress.lastCharacterId }
                if (startIndex != -1) {
                    Log.d("CharacterRepository", "Continuing from character ID: ${characters[startIndex].id}")
                    characters.subList(startIndex, characters.size)
                } else {
                    Log.d("CharacterRepository", "No more characters in this level")
                    emptyList()
                }
            } else {
                Log.d("CharacterRepository", "Last character not fully learned, continuing from it")
                val startIndex = characters.indexOfFirst { it.id >= progress.lastCharacterId }
                if (startIndex != -1) {
                    characters.subList(startIndex, characters.size)
                } else {
                    characters
                }
            }
        } else {
            Log.d("CharacterRepository", "Starting from beginning of level")
            characters
        }
    }

    suspend fun getCharactersForLevel(level: Int): List<CharacterData> {
        Log.d("CharacterRepository", "Getting characters for level: $level")
        // 获取用户的学习进度
        val progress = dao.getLearningProgressSync(currentUserId)
        val startId = if (progress != null) {
            // 从上次学习的下一个字符开始
            progress.lastCharacterId + 1
        } else {
            // 如果没有学习记录，从该级别的第一个字符开始
            (level - 1) * 10 + 1
        }

        val characters = dao.getCharactersForLevel(level, startId)
        Log.d("CharacterRepository", "Found ${characters.size} characters for level $level, starting from ID: $startId")

        return characters
    }

    suspend fun getAllCharacters(): List<CharacterData> {
        return dao.getAllCharacters()
    }

    suspend fun getMasteredCharacters() = dao.getMasteredCharacters(currentUserId)



    fun getMasteredCharactersLive(userId: Int): LiveData<List<CharacterData>> {
        return dao.getMasteredCharactersLive(userId)
    }

    suspend fun getReviewCount(): Int {
        if (currentUserId == -1) {
            throw IllegalStateException("User ID not set. Call setCurrentUser first.")
        }
        val currentTime = System.currentTimeMillis()
        // 获取需要复习的字符ID列表并去重
        return dao.getCharacterIdsForReview(currentTime, currentUserId, ).size
       // return dao.getDistinctCharactersForReview(currentUserId, currentTime)
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

    suspend fun getNextCharactersToLearn(): List<CharacterData> {
        Log.d("CharacterRepository", "Getting next characters for user: $currentUserId")

        // 获取最后学习的字的ID
        val lastLearnedId = dao.getLastLearnedCharacterId(currentUserId)
        Log.d("CharacterRepository", "Last learned character ID: $lastLearnedId")

        val characters = dao.getNextCharacters(currentUserId)
        Log.d("CharacterRepository", "Found ${characters.size} new characters to learn, starting from ID: ${characters.firstOrNull()?.id}")

        return characters
    }
}