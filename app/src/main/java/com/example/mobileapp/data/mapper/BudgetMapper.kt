package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.local.entity.BudgetEntity
import com.example.mobileapp.data.remote.BudgetDto
import com.example.mobileapp.domain.model.Budget

fun Budget.toEntity(isSynced: Boolean = false) = BudgetEntity(
    id = "${this.month}_${this.year}",
    limitAmount = this.limitAmount,
    month = this.month,
    year = this.year,
    isSynced = isSynced
)

fun BudgetEntity.toDomain() = Budget(
    id = this.id,
    limitAmount = this.limitAmount,
    month = this.month,
    year = this.year
)

fun BudgetEntity.toDto() = BudgetDto(
    id = this.id,
    limitAmount = this.limitAmount,
    month = this.month,
    year = this.year
)

fun BudgetDto.toEntity() = BudgetEntity(
    id = this.id,
    limitAmount = this.limitAmount,
    month = this.month,
    year = this.year,
    isSynced = true
)