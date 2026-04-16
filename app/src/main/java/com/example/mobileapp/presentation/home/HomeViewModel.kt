package com.example.mobileapp.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.TransactionType
import com.example.mobileapp.domain.repository.TransactionRepository
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    application: Application,
    private val repository: TransactionRepository,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val (startDate, endDate) = currentMonthRange()

        viewModelScope.launch {
            try {
                combine(
                    repository.getAllTransactions(),
                    repository.getStatistics(startDate, endDate),
                ) { transactions, stats ->
                    // Map domain.Transaction → home.model.Transaction (dùng emoji theo category)
                    val recent = transactions.take(10).map { tx ->
                        Transaction(
                            id = tx.id.toString(),
                            icon = categoryIcon(tx.category),
                            title = tx.title,
                            category = tx.category,
                            amount = if (tx.type == TransactionType.EXPENSE) -tx.amount else tx.amount,
                        )
                    }

                    // Tạo chart data: tổng chi theo từng ngày trong tháng (đơn vị nghìn đồng)
                    val chartData = buildChartData(transactions)

                    HomeState(
                        isLoading = false,
                        greeting = "Xin chào,",
                        totalBalance = stats.balance,
                        totalIncome = stats.totalIncome,
                        totalExpense = stats.totalExpense,
                        chartData = chartData,
                        recentTransactions = recent,
                    )
                }.collectLatest { newState ->
                    _state.value = newState
                }
            } catch (e: Exception) {
                _state.value = HomeState(isLoading = false, error = "Lỗi tải dữ liệu: ${e.message}")
            }
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        observeData()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Tính tổng chi theo ngày trong tháng hiện tại để vẽ biểu đồ */
    private fun buildChartData(transactions: List<com.example.mobileapp.domain.model.Transaction>): List<Float> {
        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyExpense = FloatArray(daysInMonth) { 0f }

        val (startDate, _) = currentMonthRange()
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate }

        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .forEach { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                if (txCal.get(Calendar.MONTH) == startCal.get(Calendar.MONTH) &&
                    txCal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR)
                ) {
                    val day = txCal.get(Calendar.DAY_OF_MONTH) - 1
                    dailyExpense[day] += tx.amount / 1000f // đơn vị nghìn đồng
                }
            }

        // Tích lũy để tạo đường tăng dần
        for (i in 1 until daysInMonth) {
            dailyExpense[i] += dailyExpense[i - 1]
        }

        // Lấy 8 điểm đại diện để vẽ chart
        val step = daysInMonth / 8
        return (0 until 8).map { i ->
            val idx = (i * step).coerceAtMost(daysInMonth - 1)
            dailyExpense[idx].coerceAtLeast(1f)
        }
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        return Pair(start, cal.timeInMillis)
    }

    private fun categoryIcon(category: String): String = when (category) {
        "Ăn uống"   -> "🍜"
        "Di chuyển" -> "🚕"
        "Mua sắm"   -> "🛒"
        "Hóa đơn"   -> "⚡"
        "Giải trí"  -> "🎮"
        "Sức khỏe"  -> "💊"
        "Giáo dục"  -> "📚"
        "Lương"     -> "💰"
        "Thưởng"    -> "🎁"
        "Đầu tư"    -> "📈"
        "Freelance" -> "💻"
        else        -> "📦"
    }

    // ─── Factory ─────────────────────────────────────────────────────────────

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val repo = RepositoryProvider.provideTransactionRepository(application)
            return HomeViewModel(application, repo) as T
        }
    }
}
