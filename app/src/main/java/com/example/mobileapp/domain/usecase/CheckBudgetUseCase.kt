package com.example.mobileapp.domain.usecase

import java.text.DecimalFormat

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
            percent >= 95 -> "Báo động đỏ"
            percent >= 85 -> "Cảnh báo nghiêm trọng"
            percent >= 75 -> "Cảnh báo chi tiêu"
            else           -> "An toàn"
        }
    }

    fun getStatusColor(percent: Int): String {
        return when {
            percent >= 95 -> "#F44336" // Đỏ
            percent >= 85 -> "#FF9800" // Cam
            percent >= 75 -> "#FFEB3B" // Vàng
            else           -> "#FFFFFF" // Trắng (An toàn)
        }
    }

    fun formatCurrency(amount: Long): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount).replace(",", ".") + " đ"
    }
}
