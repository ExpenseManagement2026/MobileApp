package com.example.mobileapp.data.repository

import android.content.Context
import com.example.mobileapp.data.local.database.AppDatabase
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val context: Context
) : TransactionRepository {

    // Lấy DAO động để tránh lỗi "connection pool has been closed" sau khi reset data
    private val dao get() = AppDatabase.getDatabase(context).transactionDao()

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { it.toDomainList() }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return dao.getTransactionsByType(type.name).map { it.toDomainList() }
    }

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return dao.getTransactionsByCategory(category).map { it.toDomainList() }
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return dao.searchTransactions(query).map { it.toDomainList() }
    }

    override fun getStatistics(startDate: Long, endDate: Long): Flow<Statistics> {
        val incomeFlow = dao.getTotalIncome(startDate, endDate)
        val expenseFlow = dao.getTotalExpense(startDate, endDate)
        val categoryFlow = dao.getCategoryStatistics("EXPENSE", startDate, endDate)

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

    override suspend fun insertTransaction(transaction: Transaction): Long {
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
