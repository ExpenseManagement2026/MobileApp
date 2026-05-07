package com.example.mobileapp.data.di

import android.content.Context
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.data.repository.TransactionRepositoryImpl
import com.example.mobileapp.domain.repository.TransactionRepository
import com.example.mobileapp.domain.usecase.search.FilterTransactionsUseCase

object RepositoryProvider {

    @Volatile
    private var transactionRepository: TransactionRepository? = null

    fun provideTransactionRepository(context: Context): TransactionRepository {
        return transactionRepository ?: synchronized(this) {
            val repository = TransactionRepositoryImpl(context.applicationContext)
            transactionRepository = repository
            repository
        }
    }

    fun resetRepository() {
        transactionRepository = null
        AppDatabase.closeDatabase()
    }

    val filterTransactionsUseCase by lazy { FilterTransactionsUseCase() }
}
