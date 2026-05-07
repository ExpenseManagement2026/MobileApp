package com.example.mobileapp.presentation.home.model

import java.util.Calendar

data class Transaction(
    val id: String,
    val icon: String,
    val title: String,
    val category: String,
    val amount: Long,
)

data class DayBar(
    val day: Int,       // ngày trong tháng (1-31)
    val expense: Long,  // tổng chi trong ngày (đồng)
    val isToday: Boolean = false,
)

data class SelectedMonth(
    val month: Int,  // 0-based (Calendar.MONTH)
    val year: Int,
) {
    companion object {
        fun current(): SelectedMonth {
            val cal = Calendar.getInstance()
            return SelectedMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
        }
    }
}

data class HomeState(
    val isLoading: Boolean = false,
    val greeting: String = "Xin chào,",
    val selectedMonth: SelectedMonth = SelectedMonth.current(),
    val totalBalance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val dailyBars: List<DayBar> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val error: String? = null,
)

fun Long.toVndString(): String {
    val abs = Math.abs(this)
    val formatted = abs.toString().reversed().chunked(3).joinToString(".").reversed()
    return if (this < 0) "-$formatted đ" else "$formatted đ"
}
