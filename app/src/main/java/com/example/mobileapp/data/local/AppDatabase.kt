package com.example.mobileapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Khai báo bảng Expense và phiên bản database là 1
@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Hàm để gọi các câu lệnh từ Dao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Hàm lấy Database (Singleton: đảm bảo toàn app chỉ có 1 tủ dữ liệu duy nhất)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database" // Tên file lưu trên điện thoại
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}