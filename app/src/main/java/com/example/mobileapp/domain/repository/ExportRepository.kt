package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Transaction

interface ExportRepository {
    suspend fun exportTransactionsToCSV(transactions: List<Transaction>): Result<String>
}