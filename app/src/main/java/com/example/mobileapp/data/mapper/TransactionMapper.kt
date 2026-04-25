package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.local.entity.TransactionEntity
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        title = this.title,
        amount = this.amount,
        type = this.type.name,
        category = this.category,
        date = this.date,
        note = this.note
    )
}

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = this.id,
        title = this.title,
        amount = this.amount,
        type = TransactionType.valueOf(this.type),
        category = this.category,
        date = this.date,
        note = this.note
    )
}

fun List<TransactionEntity>.toDomainList(): List<Transaction> {
    return this.map { it.toDomain() }
}

fun List<Transaction>.toEntityList(): List<TransactionEntity> {
    return this.map { it.toEntity() }
}
