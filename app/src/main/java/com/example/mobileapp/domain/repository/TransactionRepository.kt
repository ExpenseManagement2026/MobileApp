package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    // ── LOCAL CRUD ──────────────────────────────────────────────
    /** Thêm mới hoặc ghi đè một giao dịch (dùng UUID làm key). */
    suspend fun addTransaction(transaction: Transaction)

    /** Cập nhật thông tin giao dịch đã có. */
    suspend fun updateTransaction(transaction: Transaction)

    /** Xóa một giao dịch khỏi bộ nhớ cục bộ. */
    suspend fun deleteTransaction(transaction: Transaction)

    /** Lấy toàn bộ danh sách giao dịch, sắp xếp từ mới → cũ.
     *  Trả về Flow để UI tự động cập nhật khi dữ liệu thay đổi. */
    fun getAllTransactions(): Flow<List<Transaction>>

    /** Lấy tất cả giao dịch theo loại cụ thể: "INCOME" hoặc "EXPENSE". */
    fun getTransactionsByType(type: String): Flow<List<Transaction>>

    // ── SYNC SUPPORT ─────────────────────────────────────────────
    /** Lấy các giao dịch chưa được đồng bộ lên Firebase (isSynced = false). */
    suspend fun getUnsyncedTransactions(): List<Transaction>

    /** Đánh dấu giao dịch đã đồng bộ thành công (isSynced = true). */
    suspend fun markAsSynced(transactionId: String)

    // ── REMOTE ───────────────────────────────────────────────────
    /** Đẩy một giao dịch lên Firestore. Trả về Result để xử lý lỗi. */
    suspend fun pushTransactionToFirestore(transaction: Transaction): Result<Unit>

    /** Lấy toàn bộ giao dịch từ Firestore về và lưu vào Room (offline-first sync). */
    suspend fun fetchTransactionsFromFirestore(userId: String): Result<Unit>

    // ── CLEANUP ──────────────────────────────────────────────────
    /** Xóa toàn bộ dữ liệu cục bộ khi người dùng đăng xuất. */
    suspend fun clearAllLocalTransactions()
}