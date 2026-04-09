package com.example.mobileapp.data.remote

data class BudgetDto(
    val id: String = "", // MM_YYYY
    val limitAmount: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0
)