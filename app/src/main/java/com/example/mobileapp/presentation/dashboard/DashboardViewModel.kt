package com.example.mobileapp.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import com.example.mobileapp.domain.repository.TransactionRepository
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

// =============================================
// DATA MODEL - Đại diện cho 1 hạng mục chi tiêu (UI layer)
// =============================================
data class SpendingCategory(
    val name: String,
    val amount: Long,
    val colorHex: String
)

// =============================================
// UI STATE
// =============================================
data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val pieEntries: List<PieEntry> = emptyList(),
    val pieColors: List<Int> = emptyList(),
    val topCategories: List<SpendingCategory> = emptyList(),
    val allCategories: List<SpendingCategory> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val currentMonth: String = ""
)

// Bảng màu cho các danh mục
private val CATEGORY_COLORS = listOf(
    "#EF5350", "#5C6BC0", "#FFA726", "#26A69A",
    "#AB47BC", "#EC407A", "#42A5F5", "#66BB6A",
    "#FF7043", "#8D6E63"
)

// =============================================
// VIEWMODEL
// =============================================
class DashboardViewModel(
    application: Application,
    private val repository: TransactionRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    /**
     * Observe thống kê tháng hiện tại từ Room, tự động cập nhật UI khi DB thay đổi
     * Sử dụng combine để merge 2 Flow thành 1, đảm bảo data consistency
     */
    private fun observeDashboardData() {
        val (startDate, endDate) = getCurrentMonthRange()
        val currentMonth = getCurrentMonthLabel()

        viewModelScope.launch {
            try {
                // Combine statistics và transactions thành 1 Flow
                kotlinx.coroutines.flow.combine(
                    repository.getStatistics(startDate, endDate),
                    repository.getAllTransactions()
                ) { stats, allTransactions ->
                    // Filter transactions trong tháng hiện tại
                    val monthTransactions = allTransactions.filter { 
                        it.date in startDate..endDate 
                    }

                    val coloredCategories = stats.categoryBreakdown.mapIndexed { index, cat ->
                        SpendingCategory(
                            name = cat.category,
                            amount = cat.amount,
                            colorHex = CATEGORY_COLORS[index % CATEGORY_COLORS.size]
                        )
                    }

                    val pieEntries = if (coloredCategories.isNotEmpty()) {
                        coloredCategories.map { cat ->
                            val percent = if (stats.totalExpense > 0)
                                (cat.amount.toFloat() / stats.totalExpense.toFloat()) * 100f
                            else 0f
                            PieEntry(percent, cat.name)
                        }
                    } else {
                        emptyList()
                    }

                    val pieColors = coloredCategories.map {
                        android.graphics.Color.parseColor(it.colorHex)
                    }

                    DashboardUiState(
                        isLoading = false,
                        error = null,
                        totalIncome = stats.totalIncome,
                        totalExpense = stats.totalExpense,
                        balance = stats.balance,
                        pieEntries = pieEntries,
                        pieColors = pieColors,
                        topCategories = coloredCategories.take(3),
                        allCategories = coloredCategories,
                        recentTransactions = monthTransactions.take(10),
                        currentMonth = currentMonth
                    )
                }.collectLatest { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                // Xử lý lỗi, hiển thị error state
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    error = e.message ?: "Lỗi không xác định",
                    totalIncome = 0L,
                    totalExpense = 0L,
                    balance = 0L,
                    pieEntries = emptyList(),
                    pieColors = emptyList(),
                    topCategories = emptyList(),
                    allCategories = emptyList(),
                    recentTransactions = emptyList(),
                    currentMonth = currentMonth
                )
            }
        }
    }

    /**
     * Lấy giao dịch theo danh mục (realtime từ state)
     */
    fun getTransactionsByCategory(categoryName: String): List<Transaction> {
        return _uiState.value.recentTransactions.filter { 
            it.category == categoryName && it.type == TransactionType.EXPENSE
        }
    }

    /**
     * Lấy label tháng hiện tại (VD: "Tháng 4/2024")
     */
    private fun getCurrentMonthLabel(): String {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        return "Tháng $month/$year"
    }

    /**
     * Tính timestamp đầu và cuối tháng hiện tại
     */
    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startDate = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endDate = cal.timeInMillis

        return Pair(startDate, endDate)
    }

    // =============================================
    // FACTORY
    // =============================================
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val repository = RepositoryProvider.provideTransactionRepository(application)
            return DashboardViewModel(application, repository) as T
        }
    }
}
