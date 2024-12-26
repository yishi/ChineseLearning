package com.example.chineselearning.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.chineselearning.data.CharacterData
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.data.ReviewRecord
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: CharacterRepository) : ViewModel() {

    private var isImmediateReview = false
    private var currentCharacters = listOf<CharacterData>()
    private var currentIndex = 0

    fun setImmediateReview(immediate: Boolean) {
        isImmediateReview = immediate
    }

    fun getCharactersForReview(): LiveData<List<CharacterData>> = liveData {
        try {
            val characters = if (isImmediateReview) {
                Log.d("ReviewViewModel", "Getting all learned characters for immediate review")
                repository.getAllLearnedCharacters()
            } else {
                Log.d("ReviewViewModel", "Getting characters due for review")
                repository.getCharactersForReview()
            }
            Log.d("ReviewViewModel", "Retrieved ${characters.size} characters")
            currentCharacters = characters
            currentIndex = 0
            emit(characters)
        } catch (e: Exception) {
            Log.e("ReviewViewModel", "Error getting characters", e)
            emit(emptyList())
        }
    }

    fun updateReviewStatus(characterId: Int, remembered: Boolean) {
        viewModelScope.launch {
            repository.updateReviewStatus(characterId, remembered)
        }
    }

    suspend fun getAllReviewRecords(): List<ReviewRecord> {
        return repository.getAllReviewRecords()
    }

    fun getCurrentCharacter(): CharacterData? {
        return if (currentIndex < currentCharacters.size) {
            currentCharacters[currentIndex]
        } else null
    }

    fun getNextCharacter(): CharacterData? {
        currentIndex++
        return getCurrentCharacter()
    }
}

class ReviewViewModelFactory(private val repository: CharacterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}