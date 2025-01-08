package com.example.chineselearning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.chineselearning.data.AppDatabase
import com.example.chineselearning.data.User
import com.example.chineselearning.data.UserDao
import com.example.chineselearning.utils.DatabaseManager
import kotlinx.coroutines.launch
import java.security.MessageDigest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import android.view.View
import com.example.chineselearning.utils.LanguageManager

class LoginActivity : ComponentActivity() {
    private lateinit var userDao: UserDao
    private lateinit var dbManager: DatabaseManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        dbManager = DatabaseManager(this)   // 初始化 dbManager
        // 检查并请求权限
        checkAndRequestPermissions()
        // 尝试恢复数据库
        if (dbManager.restoreDatabase()) {
            Log.d("LoginActivity", "数据库恢复成功")
        }

        val database = AppDatabase.getDatabase(applicationContext)
        userDao = database.userDao()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLogin = { username, password ->
                            handleLogin(username, password)
                        },
                        onRegister = { username, password ->
                            handleRegister(username, password)
                        },
                        onAutoEnter = {
                            handleAutoRegister()
                        }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest, 1001)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // 权限获取成功，尝试恢复数据库
                if (dbManager.restoreDatabase()) {
                    Log.d("LoginActivity", "数据库恢复成功")
                }
            } else {
                // 权限被拒绝，显示提示
                Toast.makeText(this, "需要存储权限以保存学习进度", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleLogin(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("LoginActivity", "正在查询用户: $username")
                val user = userDao.getUserByUsername(username)
                    ?: run {
                        Log.d("LoginActivity", "用户不存在: $username")
                        Toast.makeText(this@LoginActivity, "用户名或密码错误", Toast.LENGTH_SHORT)
                            .show()
                        return@launch
                    }
                val passwordHash = hashPassword(password)
                Log.d("LoginActivity", "验证密码哈希值...")

                if (user.passwordHash == passwordHash) {
                    Log.d("LoginActivity", "登录成功，用户ID: ${user.id}")
                    withContext(Dispatchers.Main) {
                        // 在主线程中执行数据库备份
                        dbManager.backupDatabase()
                    }
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("userId", user.id)
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("LoginActivity", "密码错误")
                    Toast.makeText(this@LoginActivity, "用户名或密码错误", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "登录失败", e)
                Toast.makeText(this@LoginActivity, "登录失败: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun handleRegister(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@LoginActivity, "用户名已存在", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val passwordHash = hashPassword(password)
                val userId = userDao.insertUser(
                    User(
                        username = username,
                        passwordHash = passwordHash
                    )
                )

                Toast.makeText(this@LoginActivity, "注册成功", Toast.LENGTH_SHORT).show()


                // 自动登录
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("userId", userId.toInt())
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "注册失败: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun handleAutoRegister() {
        lifecycleScope.launch {
            try {
                // 生成随机用户名和密码
                // 1. 生成随机用户名：使用时间戳确保唯一性
                val randomUsername = "user_${System.currentTimeMillis()}"
                // 2. 生成6位随机密码
                val randomPassword = "pass_${(100000..999999).random()}"
                // 3. 对密码进行SHA-256哈希处理
                val passwordHash = hashPassword(randomPassword)

                // 4. 创建新用户并插入数据库
                val userId = userDao.insertUser(
                    User(
                        username = randomUsername,
                        passwordHash = passwordHash
                    )
                )
                // 5. 记录日志
                Log.d(TAG, "自动创建用户成功: $randomUsername")
                // 6. 直接进入主页，传递用户ID
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("userId", userId.toInt())
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                // 7. 错误处理
                Log.e(TAG, "自动注册失败", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "创建临时账号失败", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onAutoEnter: () -> Unit
) {
    // 使用 remember 记住状态
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    // 获取 LanguageManager 实例
    val languageManager = LanguageManager.getInstance()
    // 观察语言状态变化
    val isEnglish by languageManager.isEnglish

    Box(modifier = Modifier.fillMaxSize()) {
        // 语言切换按钮
        IconButton(
            onClick = {
                languageManager.toggleLanguage()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = if (isEnglish) "中" else "EN",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 欢迎文本
            Text(
                // text = "欢迎使用汉字学习",
                text = languageManager.getText("welcome"),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                //  label = { Text("用户名") },
                label = { Text(languageManager.getText("username")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = VisualTransformation.None
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                // label = { Text("密码") },
                label = { Text(languageManager.getText("password")) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 登录/注册按钮
            Button(
                onClick = {
                    // 处理登录逻辑
                    if (isRegistering) {
                        onRegister(username, password)
                    } else {
                        onLogin(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // 使用专门的按钮文本键
                Text(
                    if (isRegistering)
                        languageManager.getText("register_button")
                    else languageManager.getText("login_button")
                )
            }

            // 切换登录/注册文本按钮
            TextButton(
                onClick = { isRegistering = !isRegistering }
            ) {
                Text(
                    if (isRegistering)
                        languageManager.getText("login")
                    else languageManager.getText("register"),
                    color = MaterialTheme.colorScheme.primary,  // 使用主题色（通常是蓝色）
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))


            // 在切换按钮下方添加适当间距后显示说明文字
            Spacer(modifier = Modifier.height(16.dp))
            // 提示文本
            Text(
                // text = "注册/登录仅用于保存您的学习进度和复习计划\n我们不会收集任何其他个人信息",
                text = languageManager.getText("login_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 直接进入按钮
            OutlinedButton(
                onClick = onAutoEnter,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(languageManager.getText("auto_enter"))
            }

        }
    }
}




