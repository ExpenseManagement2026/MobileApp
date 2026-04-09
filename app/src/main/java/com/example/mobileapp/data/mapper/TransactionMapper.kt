package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.local.entity.TransactionEntity
import com.example.mobileapp.data.remote.TransactionDto
import com.example.mobileapp.domain.model.Transaction
import java.util.UUID

fun Transaction.toEntity(isSynced: Boolean = false) = TransactionEntity(
    id = this.id.ifEmpty { UUID.randomUUID().toString() },
    title = this.title,
    amount = this.amount,
    type = this.type,
    categoryId = this.categoryId,
    dateMillis = this.dateMillis,
    note = this.note,
    isSynced = isSynced
)

fun TransactionEntity.toDomain() = Transaction(
    id = this.id,
    title = this.title,
    amount = this.amount,
    type = this.type,
    categoryId = this.categoryId,
    dateMillis = this.dateMillis,
    note = this.note
)

fun TransactionEntity.toDto() = TransactionDto(
    id = this.id,
    title = this.title,
    amount = this.amount,
    type = this.type,
    categoryId = this.categoryId,
    dateMillis = this.dateMillis,
    note = this.note
)

fun TransactionDto.toEntity() = TransactionEntity(
    id = this.id,
    title = this.title,
    amount = this.amount,
    type = this.type,
    categoryId = this.categoryId,
    dateMillis = this.dateMillis,
    note = this.note,
    isSynced = true
)