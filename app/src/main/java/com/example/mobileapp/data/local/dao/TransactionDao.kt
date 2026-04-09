package com.example.mobileapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobileapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    // Lấy toàn bộ danh sách để đưa lên UI (Sắp xếp mới nhất lên đầu)
    @Query("SELECT * FROM transactions_table ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Dành cho Worker đồng bộ ngầm: Lấy các giao dịch chưa được đẩy lên mạng
    @Query("SELECT * FROM transactions_table WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    // Đánh dấu đã đồng bộ thành công lên Firebase
    @Query("UPDATE transactions_table SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    // Xóa sạch dữ liệu khi user Đăng xuất
    @Query("DELETE FROM transactions_table")
    suspend fun clearAll()
}