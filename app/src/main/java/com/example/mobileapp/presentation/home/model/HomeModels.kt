package com.example.mobileapp.presentation.home.model

data class Transaction(
    val id: String,
    val icon: String,
    val title: String,
    val category: String,
    val amount: Long, // âm = chi tiêu, dương = thu nhập
)

data class HomeState(
    val greeting: String = "",
    val totalBalance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val chartData: List<Float> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
)

/** -85000 → "-85.000 đ" | 15200000 → "15.200.000 đ" */
fun Long.toVndString(): String {
    val abs = Math.abs(this)
    val formatted = abs.toString().reversed().chunked(3).joinToString(".").reversed()
    return if (this < 0) "-$formatted đ" else "$formatted đ"
}
