package com.miempresa.sopaletras.data.remote.dto

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String
)

data class LoginResponseDto(
    val token: String,
    val tokenType: String?,
    val user: UserDto
)
