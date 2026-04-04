package com.example.mobileapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity: Định nghĩa đây là một bảng trong cơ sở dữ liệu Room
@Entity(tableName = "expenses")
data class Expense(
    // @PrimaryKey: Khóa chính, autoGenerate = true để ID tự tăng khi thêm mới
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,   // Tên khoản chi (ví dụ: Mua cơm trưa)
    val amount: Double,  // Số tiền (ví dụ: 35000.0)
    val date: Long,      // Ngày chi (lưu kiểu Long/Timestamp để dễ tính toán)
    val category: String // Loại: Ăn uống, Di chuyển, v.v.
)