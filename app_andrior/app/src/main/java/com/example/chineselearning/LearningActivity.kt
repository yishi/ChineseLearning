package com.example.chineselearning

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.util.Log
import android.graphics.Typeface  // 添加这行
import androidx.lifecycle.ViewModelProvider
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.data.CharacterDataList
import com.example.chineselearning.data.CharacterData
import com.example.chineselearning.databinding.ActivityLearningBinding
import com.example.chineselearning.viewmodel.LearningViewModel
import com.example.chineselearning.viewmodel.LearningViewModelFactory

data class CharacterData(
    val character: String,
    val pinyin: String,
    val meaning: String,
    val example: String,
    val level: Int // 难度级别
)

class LearningActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var characterTextView: TextView
    private lateinit var pinyinTextView: TextView
    private lateinit var meaningTextView: TextView
    private lateinit var exampleTextView: TextView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var readButton: Button
    private lateinit var tts: TextToSpeech
    private lateinit var viewModel: LearningViewModel
    private var currentIndex = 0
    private lateinit var characters: List<CharacterData>  // 添加字符列表
    private lateinit var binding: ActivityLearningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CharacterRepository(database.characterDao())
        // 使用 Factory 创建 ViewModel
        val factory = LearningViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LearningViewModel::class.java]

        // 初始化视图
        initializeViews()

        // 设置自定义字体
        setCustomFonts()

        // 初始化 TextToSpeech
        tts = TextToSpeech(this, this)

        // 使用 CharacterDataList 获取汉字数据
        val level = intent.getIntExtra("level", 1)
        characters = CharacterDataList.getCharactersByLevel(level)

        // 获取上次学习进度
        viewModel.getLastLearningProgress().observe(this) { progress ->
            progress?.let {
                currentIndex = it.lastLearnedIndex + 1  // 从上次的下一个开始
                updateDisplay()
            }
        }

        // 设置按钮点击事件
        setupButtons()
    }

    private fun initializeViews() {
        // 初始化视图
        characterTextView = findViewById(R.id.characterTextView)
        pinyinTextView = findViewById(R.id.pinyinTextView)
        meaningTextView = findViewById(R.id.meaningTextView)
        exampleTextView = findViewById(R.id.exampleTextView)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        readButton = findViewById(R.id.readButton)
    }

    private fun setCustomFonts() {
        try {
            val assetManager = assets
            val typeface = Typeface.createFromAsset(assetManager, "fonts/simkai.ttf")
            binding.apply {
                characterTextView.typeface = typeface
                // 可以选择是否为其他 TextView 也设置字体
                // pinyinTextView.typeface = typeface
                // meaningTextView.typeface = typeface
                // exampleTextView.typeface = typeface
            }
            Log.d("LearningActivity", "Font loaded successfully")
        } catch (e: Exception) {
            Log.e("LearningActivity", "Font loading failed: ${e.message}", e)
            Toast.makeText(this, "字体加载失败，使用系统默认字体", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDisplay() {
        if (characters.isNotEmpty() && currentIndex < characters.size) {
            val character = characters[currentIndex]
            characterTextView.text = character.character
            pinyinTextView.text = character.pinyin
            meaningTextView.text = character.meaning
            exampleTextView.text = character.examples

            // 更新按钮状态
            previousButton.isEnabled = currentIndex > 0
            nextButton.isEnabled = currentIndex < characters.size - 1
        }
    }

    private fun setupButtons() {
        // 下一个按钮
        nextButton.setOnClickListener {
            // 保存当前学习进度
            viewModel.saveProgress(currentIndex)
            // 标记当前汉字为已学习
            viewModel.markCharacterAsLearned(characters[currentIndex].id)
            // 显示下一个汉字
            showNextCharacter()
        }

        // 上一个按钮
        previousButton.setOnClickListener {
            showPreviousCharacter()
        }

        // 朗读按钮
        readButton.setOnClickListener {
            readCharacter(characters[currentIndex])
        }
    }

    private fun showPreviousCharacter() {
        if (currentIndex > 0) {
            currentIndex--
            updateDisplay()
        }
    }

    private fun showNextCharacter() {
        if (currentIndex < characters.size - 1) {
            currentIndex++
            updateDisplay()
        }
    }

    private fun readCharacter(character: CharacterData) {
        val textToRead = "${character.character}, ${character.examples}"
        speak(textToRead)
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun updateCharacter() {
        val character = characters[currentIndex]
        characterTextView.text = character.character
        pinyinTextView.text = character.pinyin
        meaningTextView.text = character.meaning
        exampleTextView.text = character.examples
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "请安装中文语音包", Toast.LENGTH_LONG).show()

                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        } else {
            Toast.makeText(this, "TTS初始化失败", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}