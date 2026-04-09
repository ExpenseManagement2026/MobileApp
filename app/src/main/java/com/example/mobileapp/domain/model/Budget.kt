package com.example.mobileapp.domain.model

data class Budget(
    val id: String = "", // ID sẽ tự động được map theo định dạng "MM_YYYY"
    val limitAmount: Double,
    val month: Int,
    val year: Int
)