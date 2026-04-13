package com.example.mobileapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobileapp.data.local.dao.TransactionDao
import com.example.mobileapp.data.local.entity.TransactionEntity

/**
 * =============================================
 * ROOM DATABASE - AppDatabase
 * =============================================
 * Singleton class quản lý Room Database.
 *
 * @Database annotation:
 * - entities: Danh sách các Entity (bảng) trong DB
 * - version: Version của schema (tăng lên khi thay đổi cấu trúc bảng)
 * - exportSchema: false để không export schema ra file (dùng true khi production)
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Khai báo DAO - Room sẽ tự động implement
     */
    abstract fun transactionDao(): TransactionDao

    companion object {
        /**
         * Singleton instance
         * @Volatile đảm bảo giá trị luôn được đọc từ main memory
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Lấy instance của Database (Singleton pattern)
         * synchronized để đảm bảo thread-safe
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"  // Tên file DB
                )
                    // Các config khác (nếu cần):
                    // .addMigrations(MIGRATION_1_2)  // Migration khi thay đổi schema
                    // .fallbackToDestructiveMigration()  // Xóa DB cũ nếu không có migration
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Hàm để test hoặc reset database
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
