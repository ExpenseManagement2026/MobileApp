package com.example.mobileapp.domain.usecase

class CheckBudgetUseCase {

    // 📥 Input:
    // - totalSpent: tổng tiền đã chi
    // - budget: tổng ngân sách
    // 📤 Output:
    // - % đã dùng (Int)
    fun getUsedPercent(totalSpent: Long, budget: Long): Int {

        // tránh chia cho 0
        if (budget <= 0L) return 0

        // ép Double để tính chính xác
        val percent = (totalSpent.toDouble() / budget.toDouble()) * 100

        return percent.toInt()
    }

    // 📥 Input:
    // - budget
    // - totalSpent
    // 📤 Output:
    // - tiền còn lại
    fun getRemaining(budget: Long, totalSpent: Long): Long {

        // coerceAtLeast(0) = nếu âm thì trả về 0
        return (budget - totalSpent).coerceAtLeast(0L)
    }

    // 📥 Input:
    // - percent (% đã dùng)
    // 📤 Output:
    // - trạng thái
    fun getStatus(percent: Int): String {

        return when {
            percent >= 100 -> "Vượt hạn mức"   // đỏ
            percent >= 80 -> "Sắp hết"        // cam
            else -> "An toàn"                // xanh
        }
    }
}