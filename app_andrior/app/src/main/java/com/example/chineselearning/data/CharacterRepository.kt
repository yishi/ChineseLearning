package com.example.chineselearning.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.chineselearning.utils.ReviewIntervalCalculator

class CharacterRepository(private val dao: CharacterDao) {
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
}