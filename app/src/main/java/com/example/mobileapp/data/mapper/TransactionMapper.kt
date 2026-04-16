package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.local.entity.TransactionEntity
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType

/**
 * =============================================
 * MAPPER - Chuyển đổi giữa Entity và Domain Model
 * =============================================
 * Tại sao cần Mapper?
 * - Data Layer (Entity) và Domain Layer (Model) có cấu trúc khác nhau
 * - Entity có annotation Room, Domain Model thuần túy
 * - Mapper giúp tách biệt 2 layer, dễ maintain và test
 *
 * Nguyên tắc:
 * - toEntity(): Domain Model -> Entity (để lưu vào DB)
 * - toDomain(): Entity -> Domain Model (để dùng trong business logic)
 */

/**
 * Chuyển Domain Model sang Entity
 * Dùng khi cần lưu Transaction vào Database
 */
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        title = this.title,
        amount = this.amount,
        type = this.type.name,  // Enum -> String
        category = this.category,
        date = this.date,
        note = this.note
    )
}

/**
 * Chuyển Entity sang Domain Model
 * Dùng khi đọc dữ liệu từ Database
 */
fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = this.id,
        title = this.title,
        amount = this.amount,
        type = TransactionType.valueOf(this.type),  // String -> Enum
        category = this.category,
        date = this.date,
        note = this.note
    )
}

/**
 * Extension function để map List<Entity> -> List<Domain>
 * Tiện lợi khi query nhiều records
 */
fun List<TransactionEntity>.toDomainList(): List<Transaction> {
    return this.map { it.toDomain() }
}

/**
 * Extension function để map List<Domain> -> List<Entity>
 */
fun List<Transaction>.toEntityList(): List<TransactionEntity> {
    return this.map { it.toEntity() }
}
