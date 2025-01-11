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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterData
import com.example.chineselearning.data.CharacterRepository
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.utils.LanguageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ReviewActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var repository: CharacterRepository
    private lateinit var tts: TextToSpeech
    private var currentCharacter by mutableStateOf<CharacterData?>(null)
    private var showDetails by mutableStateOf(false)
    private var isReviewMode = false
    private var userId: Int = -1
    // 添加 LanguageManager
    private val languageManager = LanguageManager.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取用户ID
        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            finish()
            return
        }

        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())
        repository.setCurrentUser(userId)  // 设置当前用户ID
        tts = TextToSpeech(this, this)

        // ... 数据库初始化代码 ...

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            // 使用语言管理器获取标题文本
                            title = { Text(languageManager.getText("review_title")) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        // 使用语言管理器获取返回按钮描述
                                        contentDescription = languageManager.getText("back")
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    // ... 布局代码 ...
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReviewCard(
                            character = currentCharacter,
                            showDetails = showDetails,
                            onRemembered = { markAsRemembered() },
                            onForgot = { showDetails = true },
                            onContinue = {
                                showDetails = false
                                markAsNotRemembered()
                            },
                            onRead = { character -> speakCharacter(character) }
                        )
                    }
                }
            }
        }

        // 加载复习数据
        loadCharactersForReview()
    }


    private fun loadCharactersForReview() {
        lifecycleScope.launch {
            try {
                val characters = repository.getCharactersForReview()
                if (characters.isNotEmpty()) {
                    currentCharacter = characters[0]
                } else {
                    Toast.makeText(this@ReviewActivity, languageManager.getText("no_review_chars"), Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error loading characters", e)
                Toast.makeText(this@ReviewActivity, languageManager.getText("review_error"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 处理"记住了"按钮点击事件
     * 1. 更新当前字符的复习状态
     * 2. 立即加载下一个待复习的字符
     */
    private fun markAsRemembered() {
        currentCharacter?.let { character ->
            lifecycleScope.launch {
                repository.updateReviewStatus(character.id, true)
                loadCharactersForReview()
            }
        }
    }

    private fun markAsNotRemembered() {
        currentCharacter?.let { character ->
            lifecycleScope.launch {
                repository.updateReviewStatus(character.id, false)
                loadCharactersForReview()
            }
        }
    }

    private fun speakCharacter(character: CharacterData) {
        val textToSpeak = if (showDetails) {
            "${character.pinyin}。${character.meaning}。${character.examples}"
        } else {
            character.character
        }
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // 修改 TTS 初始化回调
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 使用语言管理器获取提示文本
                Toast.makeText(this, languageManager.getText("tts_unavailable"), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, languageManager.getText("tts_init_failed"), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}

// 修改 ReviewCard Composable
@Composable
fun ReviewCard(
    character: CharacterData?,
    showDetails: Boolean,
    onRemembered: () -> Unit,
    onForgot: () -> Unit,
    onContinue: () -> Unit,
    onRead: (CharacterData) -> Unit
) {
    // 获取 LanguageManager 实例
    val languageManager = LanguageManager.getInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... 字符显示相关代码 ...
            character?.let { char ->
                Text(
                    text = char.character,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )

                if (showDetails) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = char.pinyin,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = char.meaning,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = char.examples,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    // 按钮区域
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 朗读按钮
                        Button(onClick = { onRead(char) }) {
                            Text(languageManager.getText("speak"))
                        }
                        // 继续按钮
                        Button(onClick = onContinue) {
                            Text(languageManager.getText("continue"))
                        }
                    }
                } else {
                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 记住了按钮
                        Button(onClick = onRemembered) {
                            Text(languageManager.getText("remembered"))
                        }
                        // 没记住按钮
                        Button(onClick = onForgot) {
                            Text(languageManager.getText("forgot"))
                        }
                        // 朗读按钮
                        Button(onClick = { onRead(char) }) {
                            Text(languageManager.getText("speak"))
                        }
                    }
                }
            } ?: Text(languageManager.getText("no_review_chars"))
        }
    }
}