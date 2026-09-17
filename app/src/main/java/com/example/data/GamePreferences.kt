package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nutbolt_master_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_LEVEL = "current_level"
        private const val KEY_HIGHEST_UNLOCKED = "highest_unlocked"
        private const val KEY_TOTAL_COINS = "total_coins"
        private const val KEY_TOTAL_SCORE = "total_score"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val PREFIX_LEVEL_STARS = "level_stars_"
        private const val PREFIX_LEVEL_HIGHSCORE = "level_highscore_"
    }

    var currentLevel: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()

    var highestUnlockedLevel: Int
        get() = prefs.getInt(KEY_HIGHEST_UNLOCKED, 1)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_UNLOCKED, value.coerceAtLeast(1)).apply()

    var totalCoins: Int
        get() = prefs.getInt(KEY_TOTAL_COINS, 100)
        set(value) = prefs.edit().putInt(KEY_TOTAL_COINS, value.coerceAtLeast(0)).apply()

    var totalScore: Int
        get() = prefs.getInt(KEY_TOTAL_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_SCORE, value.coerceAtLeast(0)).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    fun getStarsForLevel(level: Int): Int {
        return prefs.getInt(PREFIX_LEVEL_STARS + level, 0)
    }

    fun saveStarsForLevel(level: Int, stars: Int) {
        val current = getStarsForLevel(level)
        if (stars > current) {
            prefs.edit().putInt(PREFIX_LEVEL_STARS + level, stars).apply()
        }
    }

    fun getHighScoreForLevel(level: Int): Int {
        return prefs.getInt(PREFIX_LEVEL_HIGHSCORE + level, 0)
    }

    fun saveHighScoreForLevel(level: Int, score: Int) {
        val current = getHighScoreForLevel(level)
        if (score > current) {
            prefs.edit().putInt(PREFIX_LEVEL_HIGHSCORE + level, score).apply()
        }
    }

    fun unlockNextLevel(completedLevel: Int) {
        val nextLevel = completedLevel + 1
        if (nextLevel > highestUnlockedLevel && nextLevel <= 100) {
            highestUnlockedLevel = nextLevel
        }
        currentLevel = nextLevel.coerceAtMost(100)
    }

    fun isLevelUnlocked(level: Int): Boolean {
        return level <= highestUnlockedLevel
    }

    fun addCoins(amount: Int) {
        totalCoins += amount
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
        currentLevel = 1
        highestUnlockedLevel = 1
        totalCoins = 100
        totalScore = 0
        soundEnabled = true
        hapticsEnabled = true
    }
}
