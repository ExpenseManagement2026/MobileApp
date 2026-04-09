package com.example.mobileapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions_table")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String, // Dùng String (UUID) để khớp với Firebase Document ID
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" hoặc "EXPENSE"
    val categoryId: String,
    val dateMillis: Long,
    val note: String,
    val isSynced: Boolean = false
)