package com.example.mobileapp.data.repository

import com.example.mobileapp.data.local.dao.TransactionDao
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDomainList
import com.example.mobileapp.data.mapper.toEntity
import com.example.mobileapp.domain.model.CategoryAmount
import com.example.mobileapp.domain.model.Statistics
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import com.example.mobileapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * =============================================
 * REPOSITORY IMPLEMENTATION (Data Layer)
 * =============================================
 * Class này implement interface TransactionRepository từ Domain Layer.
 *
 * Nhiệm vụ:
 * - Gọi DAO để thao tác với Room Database
 * - Chuyển đổi Entity <-> Domain Model bằng Mapper
 * - Xử lý logic phức tạp (VD: tính toán Statistics từ nhiều query)
 *
 * Dependency Injection:
 * - Nhận TransactionDao qua constructor (dễ test, dễ thay đổi implementation)
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    // =============================================
    // QUERY - Đọc dữ liệu
    // =============================================

    override fun getAllTransactions(): Flow<List<Transaction>> {
        // Flow từ DAO tự động emit khi DB thay đổi
        // map {} để chuyển Entity -> Domain
        return dao.getAllTransactions().map { entities ->
            entities.toDomainList()
        }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return dao.getTransactionsByType(type.name).map { entities ->
            entities.toDomainList()
        }
    }

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return dao.getTransactionsByCategory(category).map { entities ->
            entities.toDomainList()
        }
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return dao.searchTransactions(query).map { entities ->
            entities.toDomainList()
        }
    }

    /**
     * Tính toán Statistics từ nhiều query
     * combine {} để gộp nhiều Flow thành 1 Flow
     */
    override fun getStatistics(startDate: Long, endDate: Long): Flow<Statistics> {
        // Lấy 3 Flow: totalIncome, totalExpense, categoryStats
        val incomeFlow = dao.getTotalIncome(startDate, endDate)
        val expenseFlow = dao.getTotalExpense(startDate, endDate)
        val categoryFlow = dao.getCategoryStatistics("EXPENSE", startDate, endDate)

        // Combine 3 Flow thành 1 Statistics object
        return combine(incomeFlow, expenseFlow, categoryFlow) { income, expense, categoryStats ->
            Statistics(
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                categoryBreakdown = categoryStats.map { stat ->
                    CategoryAmount(
                        category = stat.category,
                        amount = stat.totalAmount,
                        transactionCount = stat.count
                    )
                }
            )
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return dao.getTransactionById(id)?.toDomain()
    }

    // =============================================
    // INSERT / UPDATE / DELETE
    // =============================================

    override suspend fun insertTransaction(transaction: Transaction): Long {
        // Chuyển Domain -> Entity trước khi lưu vào DB
        return dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransactionById(id: Long) {
        dao.deleteTransactionById(id)
    }
}