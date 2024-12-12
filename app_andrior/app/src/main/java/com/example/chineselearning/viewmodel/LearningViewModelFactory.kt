package com.example.chineselearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chineselearning.data.CharacterRepository

class LearningViewModelFactory(
    private val repository: CharacterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LearningViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}