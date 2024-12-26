package com.example.chineselearning

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: CharacterRepository

    companion object {
        const val PREF_NAME = "ChineseLearningPrefs"
        const val KEY_LAST_LEVEL = "lastLevel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())

        // 在启动时初始化数据库
        lifecycleScope.launch {
            try {
                repository.initializeReviewRecordsIfNeeded()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database", e)
            }
        }

        // 开始学习按钮
        binding.startLearningButton.setOnClickListener {
            val sharedPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            val lastLevel = sharedPrefs.getInt(KEY_LAST_LEVEL, 1)
            val intent = Intent(this, LearningActivity::class.java)
            intent.putExtra("level", lastLevel)
            startActivity(intent)
        }

        // 开始复习按钮
        binding.startReviewButton.setOnClickListener {
            Log.d("MainActivity", "Start review button clicked")
            lifecycleScope.launch {
                try {
                    val count = repository.dao.getReviewRecordsCount()
                    if (count > 0) {
                        val intent = Intent(this@MainActivity, ReviewActivity::class.java)
                        intent.putExtra("isReviewMode", true)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "还没有学习任何汉字", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error starting review", e)
                    Toast.makeText(this@MainActivity, "启动复习时出错", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 找按钮控件
        val startLearningButton = findViewById<Button>(R.id.startLearningButton)
        val startReviewButton = findViewById<Button>(R.id.startReviewButton)
        val statisticsButton = findViewById<Button>(R.id.statisticsButton)

        // 设置点击事件
        startLearningButton.setOnClickListener {
            // 创建并启动 LearningActivity
            val intent = Intent(this, LearningActivity::class.java)
            startActivity(intent)
        }

        startReviewButton.setOnClickListener {
            // 在开始复习按钮点击事���中
            val intent = Intent(this@MainActivity, ReviewActivity::class.java)
            intent.putExtra("isReviewMode", true)
            startActivity(intent)
        }

        statisticsButton.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }
    }
}