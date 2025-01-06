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
    version = 12
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 calculated_level 列
                database.execSQL("""
                    ALTER TABLE characters ADD COLUMN calculated_level INTEGER NOT NULL DEFAULT 1
                """)

                // 更新 calculated_level 值
                database.execSQL("""
                    UPDATE characters 
                    SET calculated_level = CASE 
                        WHEN level = 1 THEN ((id - 1) / 10) + 1 
                        ELSE level 
                    END
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

    }
}