package com.example.chineselearning

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterData
import com.example.chineselearning.data.CharacterRepository
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
class LearningActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var repository: CharacterRepository
    private lateinit var tts: TextToSpeech
    private var currentLevel = 1
    private var currentCharacters = listOf<CharacterData>()
    private var currentIndex by mutableStateOf(0)
    private var currentCharacter by mutableStateOf<CharacterData?>(null)
    private var userId: Int = -1

    // 修改字体声明方式
    private val kaitiFont by lazy {
        FontFamily(
            Font(R.font.simkai)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            finish()
            return
        }

        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())
        repository.setCurrentUser(userId)

        currentLevel = intent.getIntExtra("level", 1)
        tts = TextToSpeech(this, this)

        setContent {
            MaterialTheme {

                LaunchedEffect(Unit) {
                    try {
                        loadCharacters()
                    } catch (e: Exception) {
                        Log.e("LearningActivity", "Error loading characters", e)
                        Toast.makeText(this@LearningActivity, "加载汉字时出错", Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("学习 - 第${currentLevel}级") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "返回"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 汉字显示卡片
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(), // 添加 fillMaxWidth,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center // 添加垂直居中
                            ) {
                                currentCharacter?.let { character ->
                                    // 汉字
                                    Text(
                                        text = character.character,
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontFamily = kaitiFont,
                                            color = Color.Red
                                        ),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // 拼音
                                    Text(
                                        text = character.pinyin,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // 释义
                                    Text(
                                        text = character.meaning,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // 例句
                                    Text(
                                        text = character.examples,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // 朗读按钮
                                    Button(
                                        onClick = { speakContent(character) },
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .align(Alignment.CenterHorizontally) // 确保按钮居中
                                    ) {
                                        Text("朗读")
                                    }
                                }
                            }
                        }

                        // 导航按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 上一个按钮
                            Button(
                                onClick = {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                        currentCharacter = currentCharacters[currentIndex]
                                    }
                                },
                                enabled = currentIndex > 0,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Text("上一个")
                            }

        // 下一个按钮
                            Button(
                                onClick = {
                                    if (currentIndex + 1 < currentCharacters.size) {
                                        currentIndex++
                                        currentCharacter = currentCharacters[currentIndex]
                                        lifecycleScope.launch {
                                            repository.markCharacterAsLearned(currentCharacter!!.id)
                                        }
                                    } else {
                                        Toast.makeText(this@LearningActivity, "本级别学习完成", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                },
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text("下一个")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun speakContent(character: CharacterData) {
        val textToSpeak = """
            ${character.pinyin}。
            ${character.meaning}。
            ${character.examples}
        """.trimIndent()
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "语音功能不可用", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "语音初始化失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
            tts.stop()
            tts.shutdown()
        super.onDestroy()
    }
    private fun loadCharacters() {
        lifecycleScope.launch {
            try {
                currentCharacters = repository.getCharactersByLevel(currentLevel)
                currentIndex = 0
            if (currentCharacters.isNotEmpty()) {
                currentCharacter = currentCharacters[currentIndex]
                repository.markCharacterAsLearned(currentCharacter!!.id)
            } else {
                // 当前级别学习完成，加载下一级
                lifecycleScope.launch {
                    val nextLevel = currentLevel + 1
                    val nextLevelCharacters = repository.getCharactersByLevel(nextLevel)
                    if (nextLevelCharacters.isNotEmpty()) {
                        currentLevel = nextLevel
                        currentCharacters = nextLevelCharacters
                        currentIndex = 0
                        currentCharacter = currentCharacters[0]
                        repository.markCharacterAsLearned(currentCharacter!!.id)
                    } else {
                        Toast.makeText(
                            this@LearningActivity,
                            "已完成所有汉字学习",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
            } catch (e: Exception) {
                Log.e("LearningActivity", "Error loading characters", e)
                Toast.makeText(this@LearningActivity, "加载汉字时出错", Toast.LENGTH_SHORT).show()
            }
        }
    }
}