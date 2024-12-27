package com.example.chineselearning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: CharacterRepository

    companion object {
        const val PREF_NAME = "ChineseLearningPrefs"
        const val KEY_LAST_LEVEL = "lastLevel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())

        lifecycleScope.launch {
            try {
                repository.initializeReviewRecordsIfNeeded()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database", e)
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onStartLearning = {
                            val sharedPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                            val lastLevel = sharedPrefs.getInt(KEY_LAST_LEVEL, 1)
                            val intent = Intent(this, LearningActivity::class.java)
                            intent.putExtra("level", lastLevel)
                            startActivity(intent)
                        },
                        onStartReview = {
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
                        },
                        onShowStatistics = {
                            startActivity(Intent(this, StatisticsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onStartLearning: () -> Unit,
    onStartReview: () -> Unit,
    onShowStatistics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "汉字学习",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        MainButton(
            text = "开始学习",
            onClick = onStartLearning,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MainButton(
            text = "开始复习",
            onClick = onStartReview,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MainButton(
            text = "学习统计",
            onClick = onShowStatistics
        )
    }
}

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}