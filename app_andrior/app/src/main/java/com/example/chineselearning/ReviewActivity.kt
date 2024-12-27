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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterData
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.viewmodel.ReviewViewModel
import com.example.chineselearning.viewmodel.ReviewViewModelFactory
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

class ReviewActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var viewModel: ReviewViewModel
    private var isReviewMode = false
    private var isImmediateReview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CharacterRepository(database.characterDao())
        viewModel = ViewModelProvider(this, ReviewViewModelFactory(repository))[ReviewViewModel::class.java]

        // 获取传入的复习模式
        isReviewMode = intent.getBooleanExtra("isReviewMode", false)
        isImmediateReview = intent.getBooleanExtra("isImmediateReview", false)

        // 设置复习模式
        viewModel.setImmediateReview(isImmediateReview)
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
                var showDetails by remember { mutableStateOf(false) }
                var currentCharacter by remember { mutableStateOf<CharacterData?>(null) }

                // 使用 LaunchedEffect 收集 Flow
                LaunchedEffect(Unit) {
                    viewModel.currentCharacter.collectLatest { character ->
                        currentCharacter = character
                        showDetails = false  // 重置显示状态
                    }
                }
                // 加载第一个字符
                LaunchedEffect(Unit) {
                    viewModel.loadCharactersForReview()
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (isImmediateReview) "立即复习" else "复习汉字") },
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

                                    if(showDetails) {

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
                        }

                        if (!showDetails) {
                            // 记住/没记住按钮
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        currentCharacter?.let { character ->
                                            viewModel.updateReviewStatus(character.id, true)
                                            showDetails = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Text("记住了")
                                }

                                Button(
                                    onClick = { showDetails = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                ) {
                                    Text("没记住")
                                }
                            }
                        } else {
                            // 朗读和下一个按钮
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        currentCharacter?.let { character ->
                                            speakCharacter("${character.character}. ${character.meaning}. ${character.examples}")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text("朗读")
                                }

                                Button(
                                    onClick = {
                                        currentCharacter?.let { character ->
                                            viewModel.updateReviewStatus(character.id, false)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text("下一个")
                                }
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