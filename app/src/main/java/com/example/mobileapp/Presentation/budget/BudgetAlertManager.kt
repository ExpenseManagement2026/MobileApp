package com.example.mobileapp.presentation.budget

import android.content.Context
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

class BudgetAlertManager(private val context: Context) {

    private val notificationHelper = BudgetNotificationHelper(context)
    private val useCase = CheckBudgetUseCase()
    private val repository = RepositoryProvider.provideTransactionRepository(context)

    fun checkAndNotifyAfterTransactionSaved() {
        val sharedPrefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        val budget = sharedPrefs.getLong("key_budget", 0L)

        if (budget <= 0L) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Đợi 300ms để Database kịp ghi dữ liệu mới
                delay(300)

                val calendar = Calendar.getInstance()
                val start = Calendar.getInstance().apply { 
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) 
                }.timeInMillis
                val end = Calendar.getInstance().apply { 
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) 
                }.timeInMillis

                val stats = repository.getStatistics(start, end).first()
                val totalExpense = stats.totalExpense
                val percent = useCase.getUsedPercent(totalExpense, budget)

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatVnd(value: Long): String {
        return java.text.NumberFormat.getInstance(Locale("vi", "VN")).format(value) + " đ"
    }
}
