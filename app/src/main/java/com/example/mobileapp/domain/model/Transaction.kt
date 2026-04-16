package com.example.mobileapp.domain.model

/**
 * =============================================
 * DOMAIN MODEL - Transaction (Model sạch)
 * =============================================
 * Đây là model thuần túy của Domain Layer, không phụ thuộc vào bất kỳ framework nào
 * (không có annotation Room, Gson, v.v.)
 *
 * Nguyên tắc Clean Architecture:
 * - Domain Layer là trung tâm, không biết gì về Data Layer hay Presentation Layer
 * - Model này đại diện cho nghiệp vụ thực tế: "Một giao dịch tài chính"
 */
data class Transaction(
    val id: Long = 0,                    // ID tự động tăng (0 = chưa lưu DB)
    val title: String,                   // Tiêu đề giao dịch (VD: "Mua cà phê")
    val amount: Long,                    // Số tiền (VND, dùng Long để tránh lỗi làm tròn)
    val type: TransactionType,           // Loại: Thu hay Chi
    val category: String,                // Danh mục (VD: "Ăn uống", "Lương")
    val date: Long,                      // Timestamp (milliseconds từ epoch)
    val note: String = ""                // Ghi chú thêm (optional)
)

/**
 * Enum định nghĩa loại giao dịch
 */
enum class TransactionType {
    INCOME,   // Thu nhập
    EXPENSE   // Chi tiêu
}
