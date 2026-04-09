package com.example.mobileapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets_table")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int ,
    val isSynced: Boolean = false
)