package com.example.mobileapp.presentation.budget

import android.content.Context
import android.util.Log
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.data.local.BudgetPreferences
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase
import kotlinx.coroutines.*
import java.util.*

class BudgetAlertManager(private val context: Context) {

    private val notificationHelper = BudgetNotificationHelper(context)
    private val useCase = CheckBudgetUseCase()
    private val budgetPrefs = BudgetPreferences(context)
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.transactionDao()

    /**
     * Kiểm tra chi tiêu và gửi thông báo nếu vượt ngưỡng
     */
    fun checkAndNotifyAfterTransactionSaved() {
        // 1. Lấy ngân sách
        var budget = budgetPrefs.getBudget()
        
        // Dự phòng cho key cũ của bạn nếu tháng này chưa cài
        if (budget <= 0L) {
            val oldPrefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
            budget = oldPrefs.getLong("key_budget", 0L)
        }

        Log.d("BudgetAlert", "Checking budget: $budget")
        if (budget <= 0L) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Đợi DB ghi xong hoàn toàn
                delay(800)

                // 2. Tính khoảng thời gian tháng hiện tại
                val start = Calendar.getInstance().apply { 
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = Calendar.getInstance().apply { 
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                // 3. Lấy tổng chi tiêu (Lấy trực tiếp từ DAO bằng suspend function mới)
                val totalExpense = dao.getTotalExpenseOneShot(start, end)
                val percent = useCase.getUsedPercent(totalExpense, budget)
                
                Log.d("BudgetAlert", "Total spent: $totalExpense ($percent%)")

                // 4. Gửi thông báo nếu đạt ngưỡng (>= 75%)
                if (percent >= 75) {
                    val (title, color) = when {
                        percent >= 95 -> "🚨 BÁO ĐỘNG ĐỎ: VƯỢT MỨC!" to "#F44336"
                        percent >= 85 -> "🟠 CẢNH BÁO: SẮP HẾT TIỀN!" to "#FF9800"
                        else -> "🟡 CHÚ Ý: TIÊU HẾT 75%!" to "#FFEB3B"
                    }
                    
                    notificationHelper.sendNotification(
                        title, 
                        "Bạn đã tiêu $percent% ngân sách tháng này (${formatVnd(totalExpense)}).", 
                        color
                    )
                    Log.d("BudgetAlert", "Notification sent: $title")
                }
            } catch (e: Exception) {
                Log.e("BudgetAlert", "Error checking budget", e)
            }
        }
    }

    private fun formatVnd(value: Long): String {
        return java.text.NumberFormat.getInstance(Locale("vi", "VN")).format(value) + " đ"
    }
}
