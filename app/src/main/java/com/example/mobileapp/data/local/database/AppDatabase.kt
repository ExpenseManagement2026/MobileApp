package com.example.mobileapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobileapp.data.local.dao.TransactionDao
import com.example.mobileapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

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
                    "expense_tracker_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed dữ liệu mẫu khi tạo DB lần đầu
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).transactionDao().let { dao ->
                                    seedSampleData(dao)
                                }
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedSampleData(dao: TransactionDao) {
            val cal = Calendar.getInstance()

            fun daysAgo(days: Int): Long {
                val c = cal.clone() as Calendar
                c.add(Calendar.DAY_OF_MONTH, -days)
                return c.timeInMillis
            }

            val samples = listOf(
                TransactionEntity(title = "Lương tháng", amount = 15_000_000, type = "INCOME", category = "Lương", date = daysAgo(1), note = ""),
                TransactionEntity(title = "Cơm trưa", amount = 45_000, type = "EXPENSE", category = "Ăn uống", date = daysAgo(0), note = ""),
                TransactionEntity(title = "Cafe sáng", amount = 35_000, type = "EXPENSE", category = "Ăn uống", date = daysAgo(0), note = ""),
                TransactionEntity(title = "Grab đi làm", amount = 42_000, type = "EXPENSE", category = "Di chuyển", date = daysAgo(1), note = ""),
                TransactionEntity(title = "Ăn tối nhà hàng", amount = 320_000, type = "EXPENSE", category = "Ăn uống", date = daysAgo(2), note = ""),
                TransactionEntity(title = "Áo thun Uniqlo", amount = 299_000, type = "EXPENSE", category = "Mua sắm", date = daysAgo(2), note = ""),
                TransactionEntity(title = "Xăng xe", amount = 150_000, type = "EXPENSE", category = "Di chuyển", date = daysAgo(3), note = ""),
                TransactionEntity(title = "Vé xem phim", amount = 120_000, type = "EXPENSE", category = "Giải trí", date = daysAgo(3), note = ""),
                TransactionEntity(title = "Lẩu buffet", amount = 450_000, type = "EXPENSE", category = "Ăn uống", date = daysAgo(4), note = ""),
                TransactionEntity(title = "Giày thể thao", amount = 890_000, type = "EXPENSE", category = "Mua sắm", date = daysAgo(5), note = ""),
                TransactionEntity(title = "Karaoke", amount = 200_000, type = "EXPENSE", category = "Giải trí", date = daysAgo(6), note = ""),
                TransactionEntity(title = "Tiền điện", amount = 380_000, type = "EXPENSE", category = "Hóa đơn", date = daysAgo(7), note = "")
            )
            dao.insertTransactions(samples)
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
