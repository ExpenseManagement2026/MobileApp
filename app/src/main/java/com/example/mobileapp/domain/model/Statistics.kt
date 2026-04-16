package com.example.mobileapp.domain.model

/**
 * =============================================
 * DOMAIN MODEL - Statistics
 * =============================================
 * Model đại diện cho thống kê tài chính
 */
data class Statistics(
    val totalIncome: Long,               // Tổng thu
    val totalExpense: Long,              // Tổng chi
    val balance: Long,                   // Số dư (thu - chi)
    val categoryBreakdown: List<CategoryAmount>  // Chi tiết theo danh mục
)

/**
 * Chi tiết số tiền theo từng danh mục
 */
data class CategoryAmount(
    val category: String,
    val amount: Long,
    val transactionCount: Int            // Số lượng giao dịch
)
