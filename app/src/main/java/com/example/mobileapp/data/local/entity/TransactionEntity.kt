package com.example.mobileapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * =============================================
 * ROOM ENTITY - TransactionEntity
 * =============================================
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val amount: Long,

    val type: String,

    val category: String,

    val date: Long,

    val note: String
)
