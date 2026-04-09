package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.local.entity.UserEntity
import com.example.mobileapp.data.remote.UserDto
import com.example.mobileapp.domain.model.User

fun User.toEntity(isSynced: Boolean = false) = UserEntity(
    id = this.id,
    email = this.email,
    displayName = this.displayName,
    createdAt = this.createdAt,
    isSynced = isSynced
)

fun UserEntity.toDomain() = User(
    id = this.id,
    email = this.email,
    displayName = this.displayName,
    createdAt = this.createdAt
)

fun UserEntity.toDto() = UserDto(
    uid = this.id,
    email = this.email,
    displayName = this.displayName,
    createdAt = this.createdAt
)

fun UserDto.toEntity() = UserEntity(
    id = this.uid,
    email = this.email,
    displayName = this.displayName,
    createdAt = this.createdAt,
    isSynced = true
)