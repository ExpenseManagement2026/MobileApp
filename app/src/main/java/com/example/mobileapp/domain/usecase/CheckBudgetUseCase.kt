package com.example.mobileapp.domain.usecase

import java.text.DecimalFormat

class CheckBudgetUseCase {

    /**
     * Tính % đã sử dụng
     */
    fun getUsedPercent(totalSpent: Long, budget: Long): Int {
        if (budget <= 0L) return 0
        return ((totalSpent.toDouble() / budget.toDouble()) * 100).toInt()
    }

    /**
     * Tính số tiền còn lại
     */
    fun getRemaining(budget: Long, totalSpent: Long): Long {
        return (budget - totalSpent).coerceAtLeast(0L)
    }

    /**
     * Trả về chuỗi trạng thái dựa trên % (Khớp với UI mẫu)
     */
    fun getStatus(percent: Int): String {
        return when {
            percent >= 100 -> "Vượt hạn mức"
            percent >= 80 -> "Cảnh báo"
            else -> "An toàn"
        }
    }

    /**
     * Trả về mã màu theo tone chủ đạo của thiết kế:
     * - An toàn: Xanh lá (#00BFA5)
     * - Cảnh báo: Cam (#FFA000)
     * - Nguy hiểm: Đỏ (#F44336)
     */
    fun getStatusColor(percent: Int): String {
        return when {
            percent >= 100 -> "#F44336" // Đỏ
            percent >= 80 -> "#FFA000"  // Cam (Cảnh báo)
            else -> "#00BFA5"           // Xanh chủ đạo
        }
    }

    /**
     * Định dạng tiền tệ: 10.000.000 đ
     */
    fun formatCurrency(amount: Long): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount).replace(",", ".") + " đ"
    }
}
