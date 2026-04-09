package com.example.mobileapp.domain.model

data class Category(
    val id: String = "",
    val name: String,
    val iconResName: String,
    val type: String // Dùng để lọc danh mục theo "INCOME" hoặc "EXPENSE"
)