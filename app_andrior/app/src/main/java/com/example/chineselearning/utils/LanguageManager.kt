package com.example.chineselearning.utils

// 新建文件，管理语言状态

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class LanguageManager private constructor() {
    companion object {
        private var instance: LanguageManager? = null
        fun getInstance(): LanguageManager {
            if (instance == null) {
                instance = LanguageManager()
            }
            return instance!!
        }
    }

    // 使用 MutableState 来跟踪语言状态，这样可以触发 Compose 重组
    // 默认中文
    private val _isEnglish = mutableStateOf(false)
    // 对外暴露不可变的 State
    val isEnglish: State<Boolean> = _isEnglish

    // 切换语言并触发 UI 更新
    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }
    // 获取当前语言状态
    fun isEnglish() = _isEnglish.value

    // 文本映射表
    fun getText(key: String): String {
        return when(key) {
            // 登录页面相关文本
            "login_button" -> if(isEnglish.value) "Login" else "登录"  // 专门用于登录按钮
            "register_button" -> if(isEnglish.value) "Register" else "注册"  // 专门用于注册按钮
            "login" -> if(isEnglish.value) "Already have an account? Sign in" else "已有账号？点击登录"
            "register" -> if(isEnglish.value) "New user? Create account" else "新用户？创建账号"
            "username" -> if(isEnglish.value) "Username" else "用户名"
            "password" -> if(isEnglish.value) "Password" else "密码"
            "welcome" -> if(isEnglish.value) "Welcome to Chinese Learning" else "欢迎使用汉字学习"
            "auto_enter" -> if(isEnglish.value) "Quick Start" else "直接进入"
            "login_hint" -> if(isEnglish.value)
                "Login/Register is only used to save your learning progress\nWe won't collect any other personal information"
            else
                "注册/登录仅用于保存您的学习进度和复习计划\n我们不会收集任何其他个人信息"
            // 登录页面的错误提示文本
            "username_empty" -> if(isEnglish.value) "Username cannot be empty" else "用户名不能为空"
            "password_empty" -> if(isEnglish.value) "Password cannot be empty" else "密码不能为空"
            "login_failed" -> if(isEnglish.value) "Login failed" else "登录失败"
            "register_failed" -> if(isEnglish.value) "Register failed" else "注册失败"
            // 添加主页面需要的文本映射
            // 区分顶部标题栏和主页面标题
            "app_title" -> if(isEnglish.value) "Chinese Learning" else "汉字学习"  // TopAppBar 的标题
            "main_title" -> if(isEnglish.value) "Chinese Learning" else "汉字学习"  // 主页面大标题
            "start_learning" -> if(isEnglish.value) "Start Learning" else "开始学习"
            "start_review" -> if(isEnglish.value) "Start Review" else "开始复习"
            "statistics" -> if(isEnglish.value) "Statistics" else "学习统计"
            "logout" -> if(isEnglish.value) "Logout" else "退出登录"
            // 主页面 Toast 提示信息
            "no_review_chars" -> if(isEnglish.value) "No characters need to be reviewed" else "暂无需要复习的汉字"
            "review_error" -> if(isEnglish.value) "Error starting review" else "启动复习时出错"
            "backup_success" -> if(isEnglish.value) "Database backup successful" else "数据库备份成功"
            //   "backup_failed" -> if(isEnglish.value) "Database backup failed" else "数据库备份失败"
            //   "backup_error" -> if(isEnglish.value) "Error during backup" else "备份过程出错"
            // 学习页面相关文本
            "learning_title" -> if(isEnglish.value) "Learning" else "学习"  // TopAppBar 标题
            "speak" -> if(isEnglish.value) "Speak" else "朗读"
            "previous" -> if(isEnglish.value) "Previous" else "上一个"
            "next" -> if(isEnglish.value) "Next" else "下一个"
            "back" -> if(isEnglish.value) "Back" else "返回"
            // 学习页面 Toast 提示信息
            "tts_unavailable" -> if(isEnglish.value) "Voice function is not available" else "语音功能不可用"
            "tts_init_failed" -> if(isEnglish.value) "Voice initialization failed" else "语音初始化失败"
            "load_chars_error" -> if(isEnglish.value) "Error loading characters" else "加载汉字时出错"
            "no_new_chars" -> if(isEnglish.value) "No new characters to learn" else "当前没有新的汉字可以学习"
            "all_chars_learned" -> if(isEnglish.value) "All characters have been learned" else "已完成所有汉字学习"
            "level_completed" -> if(isEnglish.value) "Current level completed" else "本级别学习完成"
            // 复习页面相关文本
            "review_title" -> if(isEnglish.value) "Review" else "复习"  // TopAppBar 标题
            "back" -> if(isEnglish.value) "Back" else "返回"  // 返回按钮
            "remembered" -> if(isEnglish.value) "Remembered" else "记住了"  // 记住了按钮
            "forgot" -> if(isEnglish.value) "Forgot" else "没记住"  // 没记住按钮
            "continue" -> if(isEnglish.value) "Continue" else "继续"  // 继续按钮
            "speak" -> if(isEnglish.value) "Speak" else "朗读"  // 朗读按钮
            // 复习页面 Toast 提示信息
            "tts_unavailable" -> if(isEnglish.value) "Voice function is not available" else "语音功能不可用"
            "tts_init_failed" -> if(isEnglish.value) "Voice initialization failed" else "语音初始化失败"
            "review_error" -> if(isEnglish.value) "Error loading review characters" else "加载复习汉字时出错"
            "no_review_chars" -> if(isEnglish.value) "No characters need to be reviewed" else "暂无需要复习的汉字"
            // 统计页面相关文本
            "statistics_title" -> if(isEnglish.value) "Statistics" else "学习统计"  // TopAppBar 标题
            "back" -> if(isEnglish.value) "Back" else "返回"  // 返回按钮
            // 统计卡片标题
            "progress_title" -> if(isEnglish.value) "Learning Progress" else "学习进度"
            "mastery_title" -> if(isEnglish.value) "Mastery Standards" else "掌握标准说明"
            // 统计数据标签
            "learned_count" -> if(isEnglish.value) "Learned" else "已学习"
            "to_review_count" -> if(isEnglish.value) "To Review" else "待复习"
            "mastered_count" -> if(isEnglish.value) "Mastered" else "已掌握"
            // 掌握标准说明文本
            "mastery_explanation" -> if(isEnglish.value)
                """A character is considered mastered when:

1. It has been reviewed at least 3 times
2. The last review was marked as "Remembered" (next review interval >12 hours)"""
            else
                """一个汉字被认为已掌握需要满足以下条件：

1. 该汉字已被复习至少3次
2. 最后一次复习时选择了"记住了"（下次复习间隔>12小时）"""
            // 统计页面错误提示
            "stats_load_error" -> if(isEnglish.value) "Error loading statistics" else "加载统计数据时出错"

            else -> ""
        }
    }
}