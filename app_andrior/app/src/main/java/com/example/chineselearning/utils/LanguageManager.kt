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
            // 登录相关文本
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
            // 错误提示文本
            "username_empty" -> if(isEnglish.value) "Username cannot be empty" else "用户名不能为空"
            "password_empty" -> if(isEnglish.value) "Password cannot be empty" else "密码不能为空"
            "login_failed" -> if(isEnglish.value) "Login failed" else "登录失败"
            "register_failed" -> if(isEnglish.value) "Register failed" else "注册失败"
            else -> ""
        }
    }
}