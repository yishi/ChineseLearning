package com.example.chineselearning

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.databinding.ActivityStatisticsBinding
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var repository: CharacterRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "学习统计"

        // 初始化数据库
        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())

        // 加载统计数据
        loadStatistics()
    }

    override fun onResume() {
        super.onResume()
        // 每次页面重新显示时更新数据
        loadStatistics()
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // 获取已学习字符数量
                val learnedCharacters = repository.getAllLearnedCharacters()
                binding.learnedCountTextView.text = learnedCharacters.size.toString()

                // 获取待复习字符数量
                val reviewCharacters = repository.getCharactersForReview()
                binding.toReviewCountTextView.text = reviewCharacters.size.toString()

                // 获取已掌握字符数量
                val masteredCharacters = repository.getMasteredCharacters()
                binding.masteredCountTextView.text = masteredCharacters.size.toString()

                // 计算总体进度
                val totalCharacters = repository.getAllCharacters().size
                val progress = if (totalCharacters > 0) {
                    ((learnedCharacters.size.toFloat() / totalCharacters.toFloat()) * 100).toInt()
                } else 0

                binding.learningProgressBar.max = 100
                binding.learningProgressBar.progress = progress
                binding.progressTextView.text = "$progress%"

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatisticsActivity, "加载统计数据失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 