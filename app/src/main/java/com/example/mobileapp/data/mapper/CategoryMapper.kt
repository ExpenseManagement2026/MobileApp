package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.local.entity.CategoryEntity
import com.example.mobileapp.data.remote.CategoryDto
import com.example.mobileapp.domain.model.Category
import java.util.UUID

fun Category.toEntity(isSynced: Boolean = false) = CategoryEntity(
    id = this.id.ifEmpty { UUID.randomUUID().toString() },
    name = this.name,
    iconResName = this.iconResName,
    type = this.type,
    isSynced = isSynced
)

fun CategoryEntity.toDomain() = Category(
    id = this.id,
    name = this.name,
    iconResName = this.iconResName,
    type = this.type
)

fun CategoryEntity.toDto() = CategoryDto(
    id = this.id,
    name = this.name,
    iconResName = this.iconResName,
    type = this.type
)

fun CategoryDto.toEntity() = CategoryEntity(
    id = this.id,
    name = this.name,
    iconResName = this.iconResName,
    type = this.type,
    isSynced = true
)