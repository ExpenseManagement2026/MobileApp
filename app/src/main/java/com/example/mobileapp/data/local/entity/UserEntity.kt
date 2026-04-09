package com.example.mobileapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Long ,
    val isSynced: Boolean = false
)
