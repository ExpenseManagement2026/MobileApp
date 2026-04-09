package com.example.mobileapp.domain.model

data class Transaction(
    val id: String = "", // Để rỗng mặc định, lúc Lưu (Mapper) sẽ tự sinh UUID
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" hoặc "EXPENSE"
    val categoryId: String,
    val dateMillis: Long,
    val note: String = ""
)