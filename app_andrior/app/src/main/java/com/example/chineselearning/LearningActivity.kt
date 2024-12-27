package com.example.chineselearning

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
class LearningActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var repository: CharacterRepository
    private lateinit var tts: TextToSpeech
    private var currentLevel = 1
    private var currentCharacters = listOf<CharacterData>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())

        currentLevel = intent.getIntExtra("level", 1)
        tts = TextToSpeech(this, this)


        // 从 assets 加载字体
        val kaitiFont = FontFamily(
            Font(
                "fonts/simkai.ttf",
                assets
            )
        )

        setContent {
            MaterialTheme {
                var currentCharacter by remember { mutableStateOf<CharacterData?>(null) }
                var currentIndex by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    try {
                        currentCharacters = repository.getCharactersByLevel(currentLevel)
                        if (currentCharacters.isNotEmpty()) {
                            currentCharacter = currentCharacters[currentIndex]
                            repository.markCharacterAsLearned(currentCharacter!!.id)
                        } else {
                            Toast.makeText(this@LearningActivity, "当前级别没有可学习的汉字", Toast.LENGTH_SHORT).show()
                            finish()
                        }
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
                                .height(400.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
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
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                }

                            }

                        }

                                    // 朗读按钮
                                    Button(
                                        onClick = {

                                            currentCharacter?.let { character ->

                                                speakCharacter("${character.character}. ${character.meaning}. ${character.examples}")

                                            }

                                        },

                                        modifier = Modifier

                                            .fillMaxWidth()

                                            .padding(vertical = 16.dp)
                                    ) {
                                        Text("朗读")

                                }


                        // 导航按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                modifier = Modifier

                                    .weight(1f)

                                    .padding(end = 8.dp)
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
                                modifier = Modifier

                                    .weight(1f)

                                    .padding(start = 8.dp)
                            ) {
                                Text("下一个")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun speakCharacter(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
}