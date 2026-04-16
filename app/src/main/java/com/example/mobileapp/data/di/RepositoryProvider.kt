package com.example.mobileapp.data.di

import android.content.Context
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.data.repository.TransactionRepositoryImpl
import com.example.mobileapp.domain.repository.TransactionRepository
import com.example.mobileapp.domain.usecase.search.FilterTransactionsUseCase

/**
 * =============================================
 * DEPENDENCY INJECTION (Manual)
 * =============================================
 * Object singleton cung cấp Repository instance.
 *
 * Trong project lớn, nên dùng Hilt/Koin để DI tự động.
 * Ở đây dùng manual DI cho đơn giản.
 *
 * Cách dùng trong ViewModel:
 * ```
 * val repository = RepositoryProvider.provideTransactionRepository(context)
 * ```
 */
object RepositoryProvider {

    @Volatile
    private var transactionRepository: TransactionRepository? = null

    /**
     * Cung cấp TransactionRepository instance (Singleton)
     */
    fun provideTransactionRepository(context: Context): TransactionRepository {
        return transactionRepository ?: synchronized(this) {
            val database = AppDatabase.getDatabase(context)
            val dao = database.transactionDao()
            val repository = TransactionRepositoryImpl(dao)
            transactionRepository = repository
            repository
        }
    }

    /**
     * Reset repository (dùng cho testing)
     */
    fun resetRepository() {
        transactionRepository = null
        AppDatabase.closeDatabase()
    }
    val filterTransactionsUseCase by lazy { FilterTransactionsUseCase() }
}
