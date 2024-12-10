package com.example.chineselearning.utils

class SpacedRepetition {
    companion object {
        // 基于艾宾浩斯遗忘曲线的复习间隔（单位：小时）
        private val REVIEW_INTERVALS = listOf(
            24,     // 第1次复习：1天后
            72,     // 第2次复习：3天后
            168,    // 第3次复习：7天后
            360,    // 第4次复习：15天后
            720     // 第5次复习：30天后
        )

        fun calculateNextReviewTime(reviewCount: Int, masteryLevel: Int): Long {
            val interval = REVIEW_INTERVALS.getOrElse(reviewCount) { REVIEW_INTERVALS.last() }
            // 根据掌握程度调整间隔
            val adjustedInterval = interval * (1.0 + (masteryLevel - 3) * 0.2)
            return System.currentTimeMillis() + (adjustedInterval * 3600000).toLong()
        }
    }
}