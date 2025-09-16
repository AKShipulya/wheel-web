package org.example.project

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

class LeaderboardManager {
    private val LEADERBOARD_KEY = "wheel_fortune_leaderboard"

    fun getLeaderboard(): List<LeaderboardEntry> {
        return try {
            val json = localStorage.getItem(LEADERBOARD_KEY)
            println("Loading leaderboard: $json") // Для отладки
            if (json != null && json.isNotEmpty()) {
                Json.decodeFromString<List<LeaderboardEntry>>(json).sortedByDescending { it.totalScore }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error loading leaderboard: ${e.message}")
            emptyList()
        }
    }

    private fun saveLeaderboard(entries: List<LeaderboardEntry>) {
        try {
            val json = Json.encodeToString(entries)
            localStorage.setItem(LEADERBOARD_KEY, json)
            println("Leaderboard saved: $json") // Для отладки
        } catch (e: Exception) {
            println("Error saving leaderboard: ${e.message}")
        }
    }

    fun updatePlayerScore(playerName: String, scoreToAdd: Int): List<LeaderboardEntry> {
        val currentLeaderboard = getLeaderboard().toMutableList()

        val existingPlayer = currentLeaderboard.find { it.name == playerName }

        if (existingPlayer != null) {
            // Игрок уже есть в лидерборде - обновляем счет
            val updatedPlayer = existingPlayer.copy(totalScore = existingPlayer.totalScore + scoreToAdd)
            val index = currentLeaderboard.indexOf(existingPlayer)
            currentLeaderboard[index] = updatedPlayer
        } else {
            // Новый игрок
            currentLeaderboard.add(LeaderboardEntry(playerName, scoreToAdd))
        }

        // Сортируем по убыванию счета
        val sortedLeaderboard = currentLeaderboard.sortedByDescending { it.totalScore }

        saveLeaderboard(sortedLeaderboard)
        return sortedLeaderboard
    }

    fun resetLeaderboard() {
        localStorage.removeItem(LEADERBOARD_KEY)
    }
}
