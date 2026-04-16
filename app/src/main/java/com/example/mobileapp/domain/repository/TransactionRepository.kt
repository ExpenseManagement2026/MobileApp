package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Statistics
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * =============================================
 * REPOSITORY INTERFACE (Domain Layer)
 * =============================================
 * Đây là "hợp đồng" giữa Domain và Data Layer.
 *
 * Nguyên tắc Dependency Inversion (SOLID):
 * - Domain Layer định nghĩa interface này
 * - Data Layer sẽ implement interface này
 * - Presentation Layer chỉ biết đến interface, không biết implementation
 *
 * Lợi ích:
 * - Dễ test (mock repository)
 * - Dễ thay đổi nguồn dữ liệu (Room -> Firebase -> API) mà không ảnh hưởng Domain
 * - Tuân thủ Clean Architecture
 */
interface TransactionRepository {

    /**
     * Lấy toàn bộ giao dịch, sắp xếp theo ngày giảm dần
     * @return Flow để UI tự động cập nhật khi DB thay đổi
     */
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Lấy giao dịch theo loại (Thu/Chi)
     * @param type Loại giao dịch cần lọc
     */
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    /**
     * Lấy giao dịch theo danh mục
     * @param category Tên danh mục
     */
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    /**
     * Tìm kiếm giao dịch theo từ khóa (title hoặc note)
     * @param query Từ khóa tìm kiếm
     */
    fun searchTransactions(query: String): Flow<List<Transaction>>

    /**
     * Lấy thống kê tài chính trong khoảng thời gian
     * @param startDate Timestamp bắt đầu
     * @param endDate Timestamp kết thúc
     */
    fun getStatistics(startDate: Long, endDate: Long): Flow<Statistics>

    /**
     * Thêm giao dịch mới
     * @param transaction Giao dịch cần thêm
     * @return ID của giao dịch vừa thêm
     */
    suspend fun insertTransaction(transaction: Transaction): Long

    /**
     * Cập nhật giao dịch
     * @param transaction Giao dịch cần cập nhật (phải có ID)
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Xóa giao dịch
     * @param transaction Giao dịch cần xóa
     */
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Xóa giao dịch theo ID
     * @param id ID của giao dịch
     */
    suspend fun deleteTransactionById(id: Long)

    /**
     * Lấy một giao dịch theo ID
     * @param id ID của giao dịch
     */
    suspend fun getTransactionById(id: Long): Transaction?
}
