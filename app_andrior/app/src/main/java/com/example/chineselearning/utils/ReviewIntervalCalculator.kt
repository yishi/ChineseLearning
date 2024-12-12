package com.example.chineselearning.utils

object ReviewIntervalCalculator {
    // 基于艾宾浩斯遗忘曲线的复习间隔（单位：小时）
    private val INTERVALS = listOf(
        24,    // 第1次复习：1天后
        72,    // 第2次复习：3天后
        168,   // 第3次复习：7天后
        360,   // 第4次复习：15天后
        720    // 第5次复习：30天后
    )

    fun calculateNextReviewTime(reviewCount: Int): Long {
        val intervalHours = if (reviewCount >= INTERVALS.size) {
            INTERVALS.last()
        } else {
            INTERVALS[reviewCount]
        }
        return System.currentTimeMillis() + (intervalHours * 3600 * 1000)
    }
}