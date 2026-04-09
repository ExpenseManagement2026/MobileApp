package com.example.mobileapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobileapp.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    // Tìm hạn mức theo ID (ID có dạng MM_YYYY)
    @Query("SELECT * FROM budgets_table WHERE id = :id")
    fun getBudget(id: String): Flow<BudgetEntity?>

    @Query("UPDATE budgets_table SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM budgets_table")
    suspend fun clearAll()
}