package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val name: String,
    val totalScore: Int
)
