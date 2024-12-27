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
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginActivity : ComponentActivity() {
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    }
                    )
                }
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
                    Toast.makeText(this@LoginActivity, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val passwordHash = hashPassword(password)
                Log.d("LoginActivity", "验证密码哈希值...")

                if (user.passwordHash == passwordHash) {
                    Log.d("LoginActivity", "登录成功，用户ID: ${user.id}")
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("userId", user.id)
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("LoginActivity", "密码错误")
                    Toast.makeText(this@LoginActivity, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "登录失败", e)
                Toast.makeText(this@LoginActivity, "登录失败: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@LoginActivity, "注册失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "欢迎使用汉字学习",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

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
            Text(if (isRegistering) "注册" else "登录")
        }

        TextButton(
            onClick = { isRegistering = !isRegistering }
        ) {
            Text(if (isRegistering) "已有账号？点击登录" else "没有账号？点击注册")
        }
    }
}

