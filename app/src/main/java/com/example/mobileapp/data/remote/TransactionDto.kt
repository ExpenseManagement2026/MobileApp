package com.example.mobileapp.data.remote

data class TransactionDto(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val categoryId: String = "",
    val dateMillis: Long = 0L,
    val note: String = ""
)