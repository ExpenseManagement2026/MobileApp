package com.example.mobileapp.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.repository.TransactionRepository
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class SpendingCategory(
    val name: String,
    val amount: Long,
    val colorHex: String,
)

data class SelectedMonth(
    val month: Int,  // 0-based (Calendar.MONTH)
    val year: Int,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val selectedMonth: SelectedMonth = run {
        val cal = Calendar.getInstance()
        SelectedMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    },
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val pieEntries: List<PieEntry> = emptyList(),
    val pieColors: List<Int> = emptyList(),
    val topCategories: List<SpendingCategory> = emptyList(),
    val allCategories: List<SpendingCategory> = emptyList(),
)

private val CATEGORY_COLORS = listOf(
    "#EF5350", "#5C6BC0", "#FFA726", "#26A69A",
    "#AB47BC", "#EC407A", "#42A5F5", "#66BB6A",
    "#FF7043", "#8D6E63"
)

class DashboardViewModel(
    application: Application,
    private val repository: TransactionRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Flow lưu tháng đang chọn để trigger reload
    private val _selectedMonth = MutableStateFlow(run {
        val cal = Calendar.getInstance()
        SelectedMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    })

    // Cache toàn bộ giao dịch để dùng cho dialog
    private var allTransactionsCache: List<Transaction> = emptyList()

    init {
        observeDashboardData()
    }

    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = SelectedMonth(month, year)
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
        // Không cho chọn tháng tương lai
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
    private fun observeDashboardData() {
        viewModelScope.launch {
            // Mỗi khi _selectedMonth thay đổi, tự động reload statistics
            _selectedMonth.flatMapLatest { selected ->
                val (startDate, endDate) = getMonthRange(selected.month, selected.year)
                repository.getStatistics(startDate, endDate)
            }.collectLatest { stats ->
                val coloredCategories = stats.categoryBreakdown.mapIndexed { index, cat ->
                    SpendingCategory(
                        name = cat.category,
                        amount = cat.amount,
                        colorHex = CATEGORY_COLORS[index % CATEGORY_COLORS.size]
                    )
                }
                val pieEntries = coloredCategories.map { cat ->
                    val percent = if (stats.totalExpense > 0)
                        (cat.amount.toFloat() / stats.totalExpense.toFloat()) * 100f
                    else 0f
                    PieEntry(percent, cat.name)
                }
                val pieColors = coloredCategories.map {
                    android.graphics.Color.parseColor(it.colorHex)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedMonth = _selectedMonth.value,
                    totalIncome = stats.totalIncome,
                    totalExpense = stats.totalExpense,
                    pieEntries = pieEntries,
                    pieColors = pieColors,
                    topCategories = coloredCategories.take(3),
                    allCategories = coloredCategories,
                )
            }
        }

        // Cache toàn bộ giao dịch để dùng cho dialog lịch sử
        viewModelScope.launch {
            repository.getAllTransactions().collectLatest { transactions ->
                allTransactionsCache = transactions
            }
        }
    }

    /** Lấy giao dịch theo category trong tháng đang chọn */
    fun getTransactionsByCategory(categoryName: String): List<Transaction> {
        val sel = _selectedMonth.value
        val (startDate, endDate) = getMonthRange(sel.month, sel.year)
        return allTransactionsCache.filter {
            it.category == categoryName && it.date in startDate..endDate
        }
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startDate = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        return Pair(startDate, cal.timeInMillis)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val repository = RepositoryProvider.provideTransactionRepository(application)
            return DashboardViewModel(application, repository) as T
        }
    }
}
