package com.example.mobileapp.data.remote

data class UserDto(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = 0L
)