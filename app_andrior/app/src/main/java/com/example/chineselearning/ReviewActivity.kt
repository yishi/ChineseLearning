package com.example.chineselearning

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chineselearning.databinding.ActivityReviewBinding
import com.example.chineselearning.data.CharacterData
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.CharacterRepository
import com.example.chineselearning.viewmodel.ReviewViewModel
import com.example.chineselearning.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.Intent

class ReviewActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityReviewBinding
    private lateinit var tts: TextToSpeech
    private lateinit var viewModel: ReviewViewModel
    private var isReviewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CharacterRepository(database.characterDao())
        viewModel = ViewModelProvider(this, ReviewViewModelFactory(repository))[ReviewViewModel::class.java]

        // 获取传入的复习模式
        isReviewMode = intent.getBooleanExtra("isReviewMode", false)
        val isImmediateReview = intent.getBooleanExtra("isImmediateReview", false)
        viewModel.setImmediateReview(isImmediateReview)

        // 设置标题
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isImmediateReview) "立即复习" else "复习汉字"

        // 初始化 TTS
        tts = TextToSpeech(this, this)

        // 加载字符
        loadCharactersForReview()
        setupButtons()
    }

    private fun loadCharactersForReview() {
        viewModel.getCharactersForReview().observe(this) { characters ->
            if (characters.isEmpty()) {
                Log.d("ReviewActivity", "No characters available for review")
                binding.characterTextView.text = "暂无需要复习的汉字"
                binding.rememberButton.visibility = View.GONE
                binding.forgotButton.visibility = View.GONE
                binding.readButton.visibility = View.GONE
            } else {
                Log.d("ReviewActivity", "Loaded ${characters.size} characters for review")
                showNextCharacter()
            }
        }
    }

    private fun loadCharactersForLearning() {
        // 保持原有的学习模式加载逻辑
        // 这里可以添加加载学习数据的代码
    }

    private fun showNoCharactersMessage() {
        binding.apply {
            characterTextView.text = if (isReviewMode) "暂无需要复习的汉字" else "暂无需要学习的汉字"
            rememberButton.visibility = View.GONE
            forgotButton.visibility = View.GONE
            readButton.visibility = View.GONE
            continueButton.visibility = View.GONE
        }
    }

    private fun showReviewCompleteMessage() {
        binding.apply {
            characterTextView.text = if (isReviewMode) "复习完成！" else "学习完成！"
            rememberButton.visibility = View.GONE
            forgotButton.visibility = View.GONE
            readButton.visibility = View.GONE
            continueButton.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        binding.apply {
            // 记住了按钮
            rememberButton.setOnClickListener {
                markAsRemembered()
            }

            // 没记住按钮
            forgotButton.setOnClickListener {
                showCharacterDetails()
            }

            // 继续复习按钮
            continueButton.setOnClickListener {
                hideCharacterDetails()
                markAsNotRemembered()
            }

            // 朗读按钮
            readButton.setOnClickListener {
                viewModel.getCurrentCharacter()?.let { character ->
                    val textToRead = "${character.character}, ${character.examples}"
                    tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
    }

    private fun showNextCharacter() {
        viewModel.getNextCharacter()?.let { character ->
            binding.apply {
                characterTextView.text = character.character
                rememberButton.visibility = View.VISIBLE
                forgotButton.visibility = View.VISIBLE
                readButton.visibility = View.VISIBLE
                continueButton.visibility = View.GONE
                
                // 隐藏详细信息
                pinyinTextView.visibility = View.GONE
                meaningTextView.visibility = View.GONE
                exampleTextView.visibility = View.GONE
            }
        } ?: run {
            // 复习完成
            showReviewCompleteMessage()
        }
    }

    private fun showCharacterDetails() {
        viewModel.getCurrentCharacter()?.let { character ->
            binding.apply {
                pinyinTextView.text = character.pinyin
                meaningTextView.text = character.meaning
                exampleTextView.text = character.examples

                pinyinTextView.visibility = View.VISIBLE
                meaningTextView.visibility = View.VISIBLE
                exampleTextView.visibility = View.VISIBLE

                // 隐藏记住和没记住按钮
                rememberButton.visibility = View.GONE
                forgotButton.visibility = View.GONE

                // 显示朗读和继续按钮
                readButton.visibility = View.VISIBLE
                continueButton.visibility = View.VISIBLE
            }
        }
    }

    private fun hideCharacterDetails() {
        binding.apply {
            pinyinTextView.visibility = View.GONE
            meaningTextView.visibility = View.GONE
            exampleTextView.visibility = View.GONE

            // 显示记住和没记住按钮
            rememberButton.visibility = View.VISIBLE
            forgotButton.visibility = View.VISIBLE

            // 隐藏朗读和继续按钮
            readButton.visibility = View.GONE
            continueButton.visibility = View.GONE
        }
    }

    private fun markAsRemembered() {
        viewModel.getCurrentCharacter()?.let { character ->
            viewModel.updateReviewStatus(character.id, true)
            showNextCharacter()
        }
    }

    private fun markAsNotRemembered() {
        viewModel.getCurrentCharacter()?.let { character ->
            viewModel.updateReviewStatus(character.id, false)
            showNextCharacter()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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