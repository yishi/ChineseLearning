package com.example.chineselearning

import android.content.Context  // 添加 Context 导入
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
import com.example.chineselearning.utils.DatabaseManager
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)  // 添加实验性 API 注解

class MainActivity : ComponentActivity() {
    private lateinit var repository: CharacterRepository
    private lateinit var dbManager: DatabaseManager

    companion object {
        const val PREF_NAME = "ChineseLearningPrefs"
        const val KEY_LAST_LEVEL = "lastLevel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbManager = DatabaseManager(this)

        val userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            // 如果没有用户ID，返回登录界面
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())
        repository.setCurrentUser(userId)  // 设置当前用户

        lifecycleScope.launch {
            try {
                repository.initializeReviewRecordsIfNeeded()
                // 验证初始化结果
                val characters = repository.getAllCharacters()
                Log.d("MainActivity", "Database contains ${characters.size} characters")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database", e)
            }
        }

        // 使用 Repository 的公共方法获取当前级别
        lifecycleScope.launch {
            val currentLevel = repository.getCurrentLevel(userId)

            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreenWithTopBar(
                            onStartLearning = {
                                val intent =
                                    Intent(this@MainActivity, LearningActivity::class.java).apply {
                                        putExtra("level", currentLevel)
                                        putExtra("userId", userId)
                                    }
                                startActivity(intent)
                            },
                            onStartReview = {
                                lifecycleScope.launch {
                                    try {
                                        // 使用 Repository 的公共方法检查复习内容
                                        val reviewCharacters = repository.getCharactersForReview()
                                        if (reviewCharacters.isNotEmpty()) {
                                            val intent = Intent(
                                                this@MainActivity,
                                                ReviewActivity::class.java
                                            )
                                            intent.putExtra("userId", userId)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "暂无需要复习的汉字",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Error starting review", e)
                                        Toast.makeText(
                                            this@MainActivity,
                                            "启动复习时出错",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onShowStatistics = {
                                val intent =
                                    Intent(this@MainActivity, StatisticsActivity::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                            },
                            onLogout = {
                                // 清除用户数据
                                getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                                    .edit()
                                    .remove("userId")
                                    .apply()

                                // 返回登录页面
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 在应用退出时备份数据库
        dbManager.backupDatabase()
    }
}

@OptIn(ExperimentalMaterial3Api::class)  // 添加实验性 API 注解
@Composable
fun MainScreenWithTopBar(
    onStartLearning: () -> Unit,
    onStartReview: () -> Unit,
    onShowStatistics: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("汉字学习") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "退出登录"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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

        // 统一三个按钮的样式
        val buttonModifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 32.dp, vertical = 8.dp)

        Button(
            onClick = onStartLearning,
            modifier = buttonModifier,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "开始学习",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }

        Button(
            onClick = onStartReview,
            modifier = buttonModifier,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "开始复习",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }

        Button(
            onClick = onShowStatistics,
            modifier = buttonModifier,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
        Text(
            text = "学习统计",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
    }
    }
}

