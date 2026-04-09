package com.example.mobileapp.domain.model

data class User(
    val id: String = "", // Đã đồng bộ dùng 'id' thay vì 'uid' như bạn thiết kế
    val email: String,
    val displayName: String,
    val createdAt: Long
)