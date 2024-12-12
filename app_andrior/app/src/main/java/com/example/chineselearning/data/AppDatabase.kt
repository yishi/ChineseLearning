package com.example.chineselearning.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.chineselearning.data.CharacterData

@Database(
    entities = [
        CharacterData::class,
        LearningProgress::class  // 添加新实体
    ],
    version = 2  // 增加数据库版本号
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 定义数据库迁移
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建新表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS learning_progress (
                        userId INTEGER PRIMARY KEY NOT NULL,
                        lastLearnedIndex INTEGER NOT NULL,
                        lastLearnedTime INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chinese_learning_database"
                )
                    .addMigrations(MIGRATION_1_2)  // 添加迁移策略
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}