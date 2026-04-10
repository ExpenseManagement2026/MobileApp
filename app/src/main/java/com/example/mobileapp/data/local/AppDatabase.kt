package com.example.mobileapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mobileapp.data.local.dao.BudgetDao
import com.example.mobileapp.data.local.dao.CategoryDao
import com.example.mobileapp.data.local.dao.TransactionDao
import com.example.mobileapp.data.local.dao.UserDao
import com.example.mobileapp.data.local.entity.TransactionEntity
import com.example.mobileapp.data.local.entity.BudgetEntity
import com.example.mobileapp.data.local.entity.CategoryEntity
import com.example.mobileapp.data.local.entity.UserEntity

@Database(
    entities = [
        TransactionEntity::class,
        UserEntity::class,
        BudgetEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
}