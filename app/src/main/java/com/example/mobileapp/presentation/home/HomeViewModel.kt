package com.example.mobileapp.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.TransactionType
import com.example.mobileapp.domain.repository.TransactionRepository
import com.example.mobileapp.presentation.home.model.DayBar
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.SelectedMonth
import com.example.mobileapp.presentation.home.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    application: Application,
    private val repository: TransactionRepository,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _selectedMonth = MutableStateFlow(SelectedMonth.current())

    init {
        observeData()
    }

    fun previousMonth() {
        val cur = _selectedMonth.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, cur.month)
            set(Calendar.YEAR, cur.year)
            add(Calendar.MONTH, -1)
        }
        _selectedMonth.value = SelectedMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }

    fun nextMonth() {
        val cur = _selectedMonth.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, cur.month)
            set(Calendar.YEAR, cur.year)
            add(Calendar.MONTH, 1)
        }
        val now = Calendar.getInstance()
        if (cal.get(Calendar.YEAR) > now.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
             cal.get(Calendar.MONTH) > now.get(Calendar.MONTH))) return
        _selectedMonth.value = SelectedMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }

    fun isCurrentMonth(): Boolean {
        val now = Calendar.getInstance()
        val sel = _selectedMonth.value
        return sel.month == now.get(Calendar.MONTH) && sel.year == now.get(Calendar.YEAR)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        val allTimeStart = 0L
        val allTimeEnd = Long.MAX_VALUE

        viewModelScope.launch {
            try {
                _selectedMonth.flatMapLatest { selected ->
                    val (startDate, endDate) = getMonthRange(selected.month, selected.year)
                    combine(
                        repository.getAllTransactions(),
                        repository.getStatistics(startDate, endDate),
                        repository.getStatistics(allTimeStart, allTimeEnd),
                    ) { transactions, monthStats, allTimeStats ->
                        val recent = transactions
                            .filter { tx ->
                                val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                                txCal.get(Calendar.MONTH) == selected.month &&
                                txCal.get(Calendar.YEAR) == selected.year
                            }
                            .take(10)
                            .map { tx ->
                                Transaction(
                                    id = tx.id.toString(),
                                    icon = categoryIcon(tx.category),
                                    title = tx.title,
                                    category = tx.category,
                                    amount = if (tx.type == TransactionType.EXPENSE) -tx.amount else tx.amount,
                                )
                            }

                        val dailyBars = buildDailyBars(transactions, selected)

                        HomeState(
                            isLoading = false,
                            greeting = "Xin chào,",
                            selectedMonth = selected,
                            totalBalance = allTimeStats.balance,
                            totalIncome = monthStats.totalIncome,
                            totalExpense = monthStats.totalExpense,
                            dailyBars = dailyBars,
                            recentTransactions = recent,
                        )
                    }
                }.collectLatest { newState ->
                    _state.value = newState
                }
            } catch (e: Exception) {
                _state.value = HomeState(isLoading = false, error = "Lỗi tải dữ liệu: ${e.message}")
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Tính tổng chi tiêu theo từng ngày trong tháng.
     * Tháng hiện tại: chỉ hiện đến hôm nay.
     * Tháng cũ: hiện cả tháng.
     */
    private fun buildDailyBars(
        transactions: List<com.example.mobileapp.domain.model.Transaction>,
        selected: SelectedMonth,
    ): List<DayBar> {
        val now = Calendar.getInstance()
        val isCurrentMonth = selected.month == now.get(Calendar.MONTH) &&
                             selected.year == now.get(Calendar.YEAR)
        val today = now.get(Calendar.DAY_OF_MONTH)

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selected.year)
            set(Calendar.MONTH, selected.month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val activeDays = if (isCurrentMonth) today else daysInMonth

        // Tổng chi theo từng ngày
        val dailyExpense = LongArray(daysInMonth) { 0L }
        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            if (txCal.get(Calendar.MONTH) == selected.month &&
                txCal.get(Calendar.YEAR) == selected.year) {
                val dayIdx = txCal.get(Calendar.DAY_OF_MONTH) - 1
                dailyExpense[dayIdx] += tx.amount
            }
        }

        return (0 until activeDays).map { i ->
            DayBar(
                day = i + 1,
                expense = dailyExpense[i],
                isToday = isCurrentMonth && (i + 1 == today),
            )
        }
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val repo = RepositoryProvider.provideTransactionRepository(application)
            return HomeViewModel(application, repo) as T
        }
    }
}
