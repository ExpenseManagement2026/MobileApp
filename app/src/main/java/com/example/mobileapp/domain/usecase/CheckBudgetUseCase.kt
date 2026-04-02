package com.example.mobileapp.domain.usecase

class CheckBudgetUseCase {

    fun getUsedPercent(totalSpent: Long, budget: Long): Int {
        if (budget <= 0L) return 0
        return ((totalSpent.toDouble() / budget.toDouble()) * 100).toInt()
    }

    fun getRemaining(budget: Long, totalSpent: Long): Long {
        return (budget - totalSpent).coerceAtLeast(0L)
    }

    fun getStatus(percent: Int): String {
        return when {
            percent >= 100 -> "Vượt hạn mức"
            percent >= 80 -> "Sắp hết"
            else -> "An toàn"
        }
    }
}