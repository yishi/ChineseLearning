package com.example.chineselearning.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context


@Database(
    entities = [
        CharacterData::class,
        LearningProgress::class,
        LearningRecord::class,
        ReviewRecord::class,
        User::class
    ],
    version = 10
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS users_backup (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, username TEXT NOT NULL, password_hash TEXT NOT NULL, createdAt INTEGER NOT NULL DEFAULT 0)")
                database.execSQL("INSERT INTO users_backup SELECT * FROM users")

                database.execSQL("DROP TABLE IF EXISTS users")
                database.execSQL("DROP TABLE IF EXISTS learning_progress")
                database.execSQL("DROP TABLE IF EXISTS learning_records")
                database.execSQL("DROP TABLE IF EXISTS review_records")
                database.execSQL("DROP TABLE IF EXISTS characters")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        username TEXT NOT NULL,
                        password_hash TEXT NOT NULL,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS learning_progress (
                        userId INTEGER PRIMARY KEY NOT NULL,
                        lastCharacterId INTEGER NOT NULL,
                        lastUpdateTime INTEGER NOT NULL,
                        currentLevel INTEGER NOT NULL DEFAULT 1
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS learning_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        character_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        learned_time INTEGER NOT NULL,
                        is_learned INTEGER NOT NULL
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS review_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        characterId INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        reviewCount INTEGER NOT NULL DEFAULT 0,
                        lastReviewTime INTEGER NOT NULL,
                        nextReviewTime INTEGER NOT NULL,
                        masteryLevel INTEGER NOT NULL DEFAULT 0
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS characters (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        character TEXT NOT NULL,
                        pinyin TEXT NOT NULL,
                        meaning TEXT NOT NULL,
                        strokes INTEGER NOT NULL,
                        examples TEXT NOT NULL,
                        level INTEGER NOT NULL DEFAULT 1
                    )
                """)

                database.execSQL("INSERT INTO users SELECT * FROM users_backup")
                database.execSQL("DROP TABLE IF EXISTS users_backup")
            }
        }
    }
}