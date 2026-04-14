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
            percent >= 100 -> "Vượt hạn mức"
            percent >= 80 -> "Cảnh báo"
            else -> "An toàn"
        }
    }

    fun getStatusColor(percent: Int): String {
        return when {
            percent >= 100 -> "#F44336" // Đỏ
            percent >= 80 -> "#FFA000"  // Cam
            else -> "#2DC98E"           // ĐÃ ĐỔI SANG XANH CỦA ĐỒNG ĐỘI
        }
    }

    fun formatCurrency(amount: Long): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount).replace(",", ".") + " đ"
    }
}
