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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ReviewActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var repository: CharacterRepository
    private lateinit var tts: TextToSpeech
    private var currentCharacter by mutableStateOf<CharacterData?>(null)
    private var showDetails by mutableStateOf(false)
    private var isReviewMode = false
    private var userId: Int = -1

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

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("复习") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "返回"
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
                    Toast.makeText(this@ReviewActivity, "暂无需要复习的汉字", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error loading characters", e)
                Toast.makeText(this@ReviewActivity, "加载数据时出错", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

@Composable
fun ReviewCard(
    character: CharacterData?,
    showDetails: Boolean,
    onRemembered: () -> Unit,
    onForgot: () -> Unit,
    onContinue: () -> Unit,
    onRead: (CharacterData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { onRead(char) }) {
                            Text("朗读")
                        }

                        Button(onClick = onContinue) {
                            Text("继续")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = onRemembered) {
                            Text("记住了")
                        }
                        Button(onClick = onForgot) {
                            Text("没记住")
                        }
                        Button(onClick = { onRead(char) }) {
                            Text("朗读")
                        }
                    }
                }
            } ?: Text("暂无需要复习的汉字")
        }
    }
}