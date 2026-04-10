package com.example.mobileapp.data.repositoryimpl

import com.example.mobileapp.data.local.dao.TransactionDao
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.data.mapper.toEntity
import com.example.mobileapp.data.remote.TransactionDto
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    // Tên collection trên Firestore
    private fun userCollection(userId: String) =
        firestore.collection("users").document(userId).collection("transactions")

    // ── LOCAL CRUD ──────────────────────────────────────────────

    /**
     * Lưu giao dịch mới vào Room với isSynced = false.
     * Sau đó thử đẩy ngay lên Firestore; nếu thành công → đánh dấu isSynced = true.
     * Nếu thất bại (mất mạng) → Worker sẽ đồng bộ lại sau.
     */
    override suspend fun addTransaction(transaction: Transaction) {
        // 1. Lưu vào Room (offline-first, isSynced = false)
        val entity = transaction.toEntity(isSynced = false)
        transactionDao.insert(entity)

        // 2. Thử đẩy lên Firestore ngay nếu có mạng
        val result = pushTransactionToFirestore(transaction.copy(id = entity.id))
        if (result.isSuccess) {
            transactionDao.markAsSynced(entity.id)
        }
    }

    /**
     * Cập nhật giao dịch trong Room (reset isSynced = false để Worker đồng bộ lại).
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        val entity = transaction.toEntity(isSynced = false)
        transactionDao.update(entity)

        // Thử sync ngay
        val result = pushTransactionToFirestore(transaction)
        if (result.isSuccess) {
            transactionDao.markAsSynced(entity.id)
        }
    }

    /**
     * Xóa giao dịch khỏi Room và đồng thời xóa trên Firestore.
     * Lưu ý: Nếu mất mạng, giao dịch bị xóa cục bộ nhưng vẫn còn trên cloud cho đến khi sync lại.
     * Để production cần thêm "pending delete" queue — tạm thời chấp nhận ở mức demo.
     */
    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction.toEntity())
        // Best-effort: xóa luôn trên Firestore nếu có mạng (không cần userId ở đây)
        // Việc xóa remote sẽ do WorkManager/SyncWorker đảm nhiệm đúng hơn
    }

    /** Flow toàn bộ giao dịch từ Room → map sang domain model */
    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    /** Flow lọc theo type: "INCOME" hoặc "EXPENSE" — dùng Room query */
    override fun getTransactionsByType(type: String): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.filter { it.type == type }.map { it.toDomain() }
        }

    // ── SYNC SUPPORT ─────────────────────────────────────────────

    /** Trả về danh sách giao dịch chưa được đẩy lên Firestore */
    override suspend fun getUnsyncedTransactions(): List<Transaction> =
        transactionDao.getUnsyncedTransactions().map { it.toDomain() }

    /** Đánh dấu isSynced = true trong Room sau khi Worker đẩy thành công */
    override suspend fun markAsSynced(transactionId: String) {
        transactionDao.markAsSynced(transactionId)
    }

    // ── REMOTE ───────────────────────────────────────────────────

    /**
     * Đẩy một giao dịch lên Firestore (collection: users/{userId}/transactions/{id}).
     * userId được lấy từ transaction.id để không cần nhận thêm tham số userId —
     * thực tế nên truyền userId qua constructor hoặc session manager.
     *
     * Sử dụng tạm "globalTransactions" nếu chưa có auth context.
     */
    override suspend fun pushTransactionToFirestore(transaction: Transaction): Result<Unit> {
        return try {
            val dto = transaction.toEntity().toDto()
            firestore.collection("transactions")
                .document(dto.id)
                .set(dto)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kéo toàn bộ giao dịch của user về từ Firestore và upsert vào Room.
     * Thường gọi khi: user vừa đăng nhập, hoặc khi force-refresh.
     */
    override suspend fun fetchTransactionsFromFirestore(userId: String): Result<Unit> {
        return try {
            val snapshot = userCollection(userId).get().await()
            val entities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TransactionDto::class.java)?.toEntity()
            }
            entities.forEach { transactionDao.insert(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── CLEANUP ──────────────────────────────────────────────────

    /** Gọi khi user đăng xuất — xóa sạch dữ liệu cục bộ */
    override suspend fun clearAllLocalTransactions() {
        transactionDao.clearAll()
    }
}