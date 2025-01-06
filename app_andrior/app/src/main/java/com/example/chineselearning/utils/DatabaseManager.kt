package com.example.chineselearning.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class DatabaseManager(private val context: Context) {
    private val DB_NAME = "chinese_learning_database"
    private val BACKUP_FILE_NAME = "chinese_learning_backup.db"

    fun backupDatabase() {
        try {
            // 获取当前数据库文件
            val currentDB = context.getDatabasePath(DB_NAME)

            // 如果数据库存在
            if (currentDB.exists()) {
                // 获取备份文件路径
                val backupFile = File(context.getExternalFilesDir(null), BACKUP_FILE_NAME)

                // 复制数据库文件到备份文件
                FileInputStream(currentDB).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("DatabaseManager", "数据库备份成功: ${backupFile.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e("DatabaseManager", "数据库备份失败", e)
        }
    }

    fun restoreDatabase(): Boolean {
        try {
            // 获取备份文件
            val backupFile = File(context.getExternalFilesDir(null), BACKUP_FILE_NAME)

            // 如果备份文件存在
            if (backupFile.exists()) {
                // 获取当前数据库文件
                val currentDB = context.getDatabasePath(DB_NAME)

                // 确保数据库目录存在
                currentDB.parentFile?.mkdirs()

                // 复制备份文件到数据库文件
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(currentDB).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("DatabaseManager", "数据库恢复成功")
                return true
            }
        } catch (e: IOException) {
            Log.e("DatabaseManager", "数据库恢复失败", e)
        }
        return false
    }
}