package com.example.mobileapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * =============================================
 * ROOM ENTITY - TransactionEntity
 * =============================================
 * Đây là model của Data Layer, chứa annotation Room để map với SQLite.
 *
 * Tại sao tách Entity và Domain Model?
 * - Entity phụ thuộc vào Room (framework Android)
 * - Domain Model hoàn toàn độc lập, có thể dùng ở backend, shared module, v.v.
 * - Khi thay đổi DB (Room -> Realm), chỉ cần sửa Data Layer
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val amount: Long,

    /**
     * Lưu dạng String trong DB để dễ query
     * "INCOME" hoặc "EXPENSE"
     */
    val type: String,

    val category: String,

    /**
     * Timestamp (Long) để dễ so sánh và sắp xếp
     */
    val date: Long,

    val note: String
)