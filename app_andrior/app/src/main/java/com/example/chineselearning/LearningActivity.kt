package com.example.chineselearning

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.util.Log
import android.graphics.Typeface
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.data.CharacterData
import com.example.chineselearning.databinding.ActivityLearningBinding
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class LearningActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var binding: ActivityLearningBinding
    private lateinit var repository: CharacterRepository
    private var currentLevel = 1
    private var currentCharacters = listOf<CharacterData>()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化数据库和仓库
        val database = AppDatabase.getDatabase(applicationContext)
        repository = CharacterRepository(database.characterDao())

        // 获取传入的级别
        currentLevel = intent.getIntExtra("level", 1)

        // 初始化 TTS
        tts = TextToSpeech(this, this)

        // 设置自定义字体
        setCustomFonts()

        // 加载当前级别的汉字
        loadCharactersForCurrentLevel()
        setupButtons()
    }

    private fun loadCharactersForCurrentLevel() {
        lifecycleScope.launch {
            try {
                currentCharacters = repository.getCharactersByLevel(currentLevel)
                if (currentCharacters.isNotEmpty()) {
                    showCurrentCharacter()
                } else {
                    Toast.makeText(this@LearningActivity, "当前级别没有可学习的汉字", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("LearningActivity", "Error loading characters", e)
                Toast.makeText(this@LearningActivity, "加载汉字时出错", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showCurrentCharacter() {
        if (currentIndex < currentCharacters.size) {
            val character = currentCharacters[currentIndex]
            binding.apply {
                characterTextView.text = character.character
                pinyinTextView.text = character.pinyin
                meaningTextView.text = character.meaning
                exampleTextView.text = character.examples
                strokesTextView.text = "笔画：${character.strokes}"
            }
        }
    }

    private fun setCustomFonts() {
        try {
            val assetManager = assets
            val typeface = Typeface.createFromAsset(assetManager, "fonts/simkai.ttf")
            binding.apply {
                characterTextView.typeface = typeface
            }
            Log.d("LearningActivity", "Font loaded successfully")
        } catch (e: Exception) {
            Log.e("LearningActivity", "Font loading failed: ${e.message}", e)
            Toast.makeText(this, "字体加载失败，使用系统默认字体", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        binding.apply {
            // 下一个按钮
            nextButton.setOnClickListener {
                if (currentIndex < currentCharacters.size - 1) {
                    currentIndex++
                    showCurrentCharacter()
                }
            }

            // 上一个按钮
            previousButton.setOnClickListener {
                if (currentIndex > 0) {
                    currentIndex--
                    showCurrentCharacter()
                }
            }

            // 朗读按钮
            readButton.setOnClickListener {
                val character = currentCharacters.getOrNull(currentIndex)
                character?.let {
                    val textToRead = "${it.character}, ${it.examples}"
                    tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "")
                }
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
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

    private fun saveCurrentLevel() {
        val sharedPrefs = getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE)
        sharedPrefs.edit().putInt(MainActivity.KEY_LAST_LEVEL, currentLevel).apply()
    }

    override fun onPause() {
        super.onPause()
        saveCurrentLevel()
    }
}