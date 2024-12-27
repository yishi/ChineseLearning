package com.example.chineselearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.data.CharacterData
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData

class StatisticsActivity : ComponentActivity() {
    private lateinit var repository: CharacterRepository
    private var currentUserId: Int = -1

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUserId = intent.getIntExtra("userId", -1)
        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())
        repository.setCurrentUser(currentUserId)

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("学习统计") },
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
                    StatisticsScreen(
                        repository = repository,
                        userId = currentUserId,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsScreen(
    repository: CharacterRepository,
    userId: Int,
    modifier: Modifier = Modifier
) {
    val learnedCharacters by repository.getAllLearnedCharactersLive(userId)
        .observeAsState(initial = emptyList())
    val masteredCharacters by repository.getMasteredCharactersLive(userId)
        .observeAsState(initial = emptyList())
    var toReviewCharacters by remember { mutableStateOf(0) }
    val progress = (learnedCharacters.size.toFloat() / 243) * 100

    LaunchedEffect(Unit) {
        // 加载待复习数据
        repository.getCharactersForReview().let { review ->
            toReviewCharacters = review.size
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            StatisticsCard(
                learnedCount = learnedCharacters.size,
                toReviewCount = toReviewCharacters,
                masteredCount = masteredCharacters.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressCard(progress = progress)

            Spacer(modifier = Modifier.height(16.dp))

            ExplanationCard()
        }
    }
}

@Composable
fun StatisticsCard(
    learnedCount: Int,
    toReviewCount: Int,
    masteredCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "学习统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("已学习", learnedCount)
                StatItem("待复习", toReviewCount)
                StatItem("已掌握", masteredCount)
            }
        }
    }
}

@Composable
fun ProgressCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "学习进度",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = progress / 100,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${progress.toInt()}%",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "掌握标准说明",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = """一个汉字被认为已掌握需要满足以下条件：

1. 该汉字已被复习至少3次
2. 最后一次复习时选择了"记住了"（下次复习间隔>12小时）""".trimIndent(),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.5
            )
        }
    }
}

@Composable
fun StatItem(label: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}