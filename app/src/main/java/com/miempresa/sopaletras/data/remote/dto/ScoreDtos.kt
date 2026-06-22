package com.miempresa.sopaletras.data.remote.dto

data class CreateScoreRequestDto(
    val score: Int,
    val durationSeconds: Int
)

data class ScoreDto(
    val id: String,
    val userId: String,
    val username: String?,
    val score: Int,
    val durationSeconds: Int,
    val createdAt: String
)
