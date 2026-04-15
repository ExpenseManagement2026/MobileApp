package com.example.mobileapp.data.local.dao

import androidx.room.*
import com.example.mobileapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * =============================================
 * ROOM DAO - Data Access Object
 * =============================================
 * Interface định nghĩa các câu lệnh SQL để tương tác với bảng transactions.
 * Room sẽ tự động generate implementation.
 */
@Dao
interface TransactionDao {

    // =============================================
    // QUERY - Đọc dữ liệu
    // =============================================

    /**
     * Lấy toàn bộ giao dịch, sắp xếp theo ngày mới nhất
     * Flow tự động emit giá trị mới khi DB thay đổi
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Lấy giao dịch theo loại (INCOME/EXPENSE)
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    /**
     * Lấy giao dịch theo danh mục
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    /**
     * Tìm kiếm giao dịch theo từ khóa (title hoặc note)
     * LIKE '%query%' để tìm kiếm gần đúng
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE title LIKE '%' || :query || '%' 
           OR note LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    /**
     * Lấy giao dịch trong khoảng thời gian
     * Dùng cho Dashboard và Budget
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * Tính tổng thu trong khoảng thời gian
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Long>

    /**
     * Tính tổng chi trong khoảng thời gian
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Long>

    /**
     * Thống kê theo danh mục (cho PieChart)
     * GROUP BY category, tính tổng và đếm số lượng
     */
    @Query("""
        SELECT category, SUM(amount) as totalAmount, COUNT(*) as count
        FROM transactions 
        WHERE type = :type AND date BETWEEN :startDate AND :endDate
        GROUP BY category
        ORDER BY totalAmount DESC
    """)
    fun getCategoryStatistics(type: String, startDate: Long, endDate: Long): Flow<List<CategoryStatResult>>

    /**
     * Lấy một giao dịch theo ID
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    // =============================================
    // INSERT - Thêm dữ liệu
    // =============================================

    /**
     * Thêm giao dịch mới
     * @return ID của giao dịch vừa thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * Thêm nhiều giao dịch cùng lúc
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    // =============================================
    // UPDATE - Cập nhật dữ liệu
    // =============================================

    /**
     * Cập nhật giao dịch
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    // =============================================
    // DELETE - Xóa dữ liệu
    // =============================================

    /**
     * Xóa giao dịch
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * Xóa giao dịch theo ID
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    /**
     * Xóa toàn bộ giao dịch (dùng cho testing hoặc reset app)
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

/**
 * Data class để nhận kết quả từ query GROUP BY
 * Room tự động map các cột vào properties
 */
data class CategoryStatResult(
    val category: String,
    val totalAmount: Long,
    val count: Int
)
