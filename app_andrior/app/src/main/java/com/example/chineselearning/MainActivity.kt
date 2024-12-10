package com.example.chineselearning

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 找到按钮控件
        val startLearningButton = findViewById<Button>(R.id.startLearningButton)
        val startReviewButton = findViewById<Button>(R.id.startReviewButton)
        val statisticsButton = findViewById<Button>(R.id.statisticsButton)

        // 设置点击事件
        startLearningButton.setOnClickListener {
            // 创建并启动 LearningActivity
            val intent = Intent(this, LearningActivity::class.java)
            startActivity(intent)
        }

        startReviewButton.setOnClickListener {
            Toast.makeText(this, "开始复习（功能开发中...）", Toast.LENGTH_SHORT).show()
            // TODO: 跳转到复习界面
        }

        statisticsButton.setOnClickListener {
            Toast.makeText(this, "学习统计（功能开发中...）", Toast.LENGTH_SHORT).show()
            // TODO: 跳转到统计界面
        }
    }
}