package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {


    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transaction: Transaction)


}
