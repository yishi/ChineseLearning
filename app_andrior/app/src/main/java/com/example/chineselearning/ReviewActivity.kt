package com.example.chineselearning

import android.content.Intent
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
import android.provider.Settings
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.TextToSpeech.Engine

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

    // 添加对话框显示状态
    private var showLanguageDialog by mutableStateOf(false)
    private var showSettingsDialog by mutableStateOf(false)


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
//        tts = TextToSpeech(this, this)

        // 初始化 TTS，添加详细日志
        try {
            Log.d("TTS", "Initializing TTS...")
            tts = TextToSpeech(this, this).apply {
                // 设置语言为中文
                language = Locale.CHINESE
                // 设置语速和音调
                setSpeechRate(1.0f)
                setPitch(1.0f)
            }
            Log.d("TTS", "TTS instance created")
        } catch (e: Exception) {
            Log.e("TTS", "Error creating TTS instance", e)
        }

        // ... 数据库初始化代码 ...

        setContent {
            MaterialTheme {
                Box {
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

                    if (showLanguageDialog) {
                        TTSLanguageDialog(
                            onDismiss = { showLanguageDialog = false },
                            onConfirm = {
                                showLanguageDialog = false
                                startActivity(Intent().apply {
                                    action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                                })
                            }
                        )
                    }

                    if (showSettingsDialog) {
                        TTSSettingsDialog(
                            onDismiss = { showSettingsDialog = false },
                            onConfirm = {
                                showSettingsDialog = false
                                startActivity(Intent().apply {
                                    action = "com.android.settings.TTS_SETTINGS"
                                })
                            }
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
                    Toast.makeText(
                        this@ReviewActivity,
                        languageManager.getText("no_review_chars"),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error loading characters", e)
                Toast.makeText(
                    this@ReviewActivity,
                    languageManager.getText("review_error"),
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun initTTS() {
        try {
            Log.d("TTS", "Initializing TTS...")


            tts = TextToSpeech(applicationContext, this).apply {
            // 获取系统可用的 TTS 引擎列表
            val ttsEngines = engines
            Log.d("TTS", "Available TTS engines: ${
                ttsEngines.joinToString { engine ->
                    "${engine.name}(${engine.label})"
                }
            }")

                // 使用系统默认引擎
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("TTS", "Started speaking: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d("TTS", "Finished speaking: $utteranceId")
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e("TTS", "Error speaking: $utteranceId")
                    }
                })

            }
            Log.d("TTS", "TTS instance created")
        } catch (e: Exception) {
            Log.e("TTS", "Error creating TTS instance", e)
        }
    }

    // 修改 TTS 初始化回调
    override fun onInit(status: Int) {

        Log.d("TTS", "TTS initialization status: $status")

        if (status == TextToSpeech.SUCCESS) {
            // 获取当前引擎
            val defaultEngine = tts.defaultEngine
            Log.d("TTS", "Default TTS engine: $defaultEngine")

            // 检查可用的语言
            val availableLocales = tts.availableLanguages
            Log.d("TTS", "Available languages: $availableLocales")

            // 设置中文
            val result = tts.setLanguage(Locale.CHINESE)
            Log.d("TTS", "Set language result: $result")

            /*            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 使用语言管理器获取提示文本
                Toast.makeText(this, languageManager.getText("tts_unavailable"), Toast.LENGTH_SHORT).show()
            }*/

            when (result) {
                TextToSpeech.LANG_AVAILABLE -> {
                    Log.d("TTS", "Language is available")
                    // 测试语音功能
                    tts.speak("测试", TextToSpeech.QUEUE_FLUSH, null, "test")
                }
                TextToSpeech.LANG_COUNTRY_AVAILABLE -> {
                    Log.d("TTS", "Language country is available")
                    // 尝试使用备选语言
                    tryAlternativeLanguage()
                }

                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.e("TTS", "Language data missing")
                    // 提示用户安装语言包
                    showLanguageDialog = true
                }

                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.e("TTS", "Language not supported")
                    // 尝试使用备选语言
                    tryAlternativeLanguage()
                }

                else -> {
                    Log.d("TTS", "TTS initialized successfully")
                }
            }
        } else {
//            Toast.makeText(this, languageManager.getText("tts_init_failed"), Toast.LENGTH_SHORT).show()
            Log.e("TTS", "TTS initialization failed with status: $status")
            // 提示用户检查 TTS 设置
            showSettingsDialog = true
        }
    }

    // 尝试使用备选语言
    private fun tryAlternativeLanguage() {
        val availableLocales = listOf(
            Locale.CHINESE,
            Locale.SIMPLIFIED_CHINESE,
            Locale.TRADITIONAL_CHINESE,
            Locale("zh", "CN"),
            Locale("zh", "TW")
        )

        for (locale in availableLocales) {
            val result = tts.setLanguage(locale)
            Log.d("TTS", "Trying language $locale, result: $result")
            if (result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS", "Using alternative language: $locale")
                return
            }
        }

        Log.e("TTS", "Failed to set any Chinese language variant")
        // 如果所有语言都不可用，显示提示
        showLanguageDialog = true
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

}

@Composable
private fun TTSLanguageDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("语音功能提示") },
        text = { Text("需要安装中文语音包才能使用朗读功能。是否前往安装？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("去安装")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun TTSSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("语音功能提示") },
        text = { Text("语音功能初始化失败。是否前往系统设置检查？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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