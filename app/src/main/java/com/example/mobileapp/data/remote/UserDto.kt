package com.example.mobileapp.data.remote

data class UserDto(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = 0L
)