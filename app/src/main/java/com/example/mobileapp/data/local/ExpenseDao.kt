package com.example.mobileapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    // Lấy toàn bộ danh sách chi tiêu, ngày mới nhất xếp lên đầu
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    // Thêm khoản chi mới. Nếu trùng ID thì ghi đè dữ liệu cũ.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    // Xóa một khoản chi cụ thể
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Tính tổng số tiền đã chi
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalAmount(): Flow<Double?>
}