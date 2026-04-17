package com.example.mobileapp.presentation.home.model

data class Transaction(
    val id: String,
    val icon: String,
    val title: String,
    val category: String,
    val amount: Long,
)

data class ChartPoint(
    val day: Int,       // ngày trong tháng (1-31)
    val amount: Float,  // tổng chi tích lũy (đơn vị: nghìn đồng)
)

data class HomeState(
    val isLoading: Boolean = false,
    val greeting: String = "Xin chào,",
    val totalBalance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val chartData: List<Float> = emptyList(),
    val chartPoints: List<ChartPoint> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val error: String? = null,
)

fun Long.toVndString(): String {
    val abs = Math.abs(this)
    val formatted = abs.toString().reversed().chunked(3).joinToString(".").reversed()
    return if (this < 0) "-$formatted đ" else "$formatted đ"
}
