package com.example.chineselearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.data.LearningProgress

class LearningViewModel(
    private val repository: CharacterRepository  // 通过构造函数注入
) : ViewModel() {
    private val userId: Int = 1 // 暂时硬编码用户ID，后续可以通过依赖注入提供

    fun getLastLearningProgress(): LiveData<LearningProgress> {
        return repository.getLearningProgress(userId)
    }

    fun saveProgress(currentIndex: Int) {
        viewModelScope.launch {
            repository.saveLearningProgress(
                LearningProgress(
                    userId = userId,
                    lastLearnedIndex = currentIndex,
                    lastLearnedTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun markCharacterAsLearned(characterId: Int) {
        viewModelScope.launch {
            repository.markCharacterAsLearned(characterId)
        }
    }
}