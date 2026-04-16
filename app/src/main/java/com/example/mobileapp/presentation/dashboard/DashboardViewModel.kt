package com.example.mobileapp.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.example.mobileapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// =============================================
// DATA MODEL - Đại diện cho 1 hạng mục chi tiêu
// =============================================
data class SpendingCategory(
    val name: String,       // Tên hạng mục
    val amount: Long,       // Số tiền (VND)
    val colorHex: String    // Màu hiển thị (hex string)
)

// =============================================
// DATA MODEL - Đại diện cho 1 giao dịch chi tiêu
// =============================================
data class Transaction(
    val id: String,
    val categoryName: String,
    val description: String,
    val amount: Long,
    val date: String  // Format: "dd/MM/yyyy"
)

// =============================================
// UI STATE - Trạng thái toàn bộ màn hình Dashboard
// Đây là "single source of truth" mà Fragment sẽ observe
// =============================================
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalIncome: Long = 0L,         // Tổng thu
    val totalExpense: Long = 0L,        // Tổng chi
    val pieEntries: List<PieEntry> = emptyList(),           // Dữ liệu cho PieChart
    val pieColors: List<Int> = emptyList(),                 // Màu tương ứng từng slice
    val topCategories: List<SpendingCategory> = emptyList(), // Top 3 hạng mục chi nhiều nhất
    val allCategories: List<SpendingCategory> = emptyList()  // Toàn bộ danh mục
)

// =============================================
// VIEWMODEL - Tầng xử lý logic UI (MVVM Layer)
// Không giữ reference đến View/Fragment
// =============================================
class DashboardViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Lấy lịch sử giao dịch theo danh mục (Real time từ DB)
     */
    fun getTransactionsByCategoryFlow(categoryName: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCategory(categoryName).map { list ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            list.map { trans ->
                Transaction(
                    id = trans.id.toString(),
                    categoryName = trans.category,
                    description = trans.title,
                    amount = trans.amount,
                    date = sdf.format(Date(trans.date))
                )
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = calendar.timeInMillis

            repository.getStatistics(startDate, endDate).collect { stats ->
                val totalExpense = stats.totalExpense
                val totalIncome = stats.totalIncome
                // Bảng màu chuẩn
                val colors = listOf("#EF5350", "#5C6BC0", "#FFA726", "#26A69A", "#AB47BC", "#8D6E63", "#26C6DA")

                val categories = stats.categoryBreakdown.mapIndexed { index, cat ->
                    SpendingCategory(
                        name = cat.category,
                        amount = cat.amount,
                        colorHex = colors[index % colors.size]
                    )
                }.sortedByDescending { it.amount }

                val pieEntries = categories.map { cat ->
                    PieEntry(
                        if (totalExpense > 0) (cat.amount.toFloat() / totalExpense.toFloat()) * 100f else 0f,
                        cat.name
                    )
                }

                val pieColors = categories.map {
                    android.graphics.Color.parseColor(it.colorHex)
                }

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    pieEntries = pieEntries,
                    pieColors = pieColors,
                    topCategories = categories.take(3),
                    allCategories = categories
                )
            }
        }
    }

    // Builder định nghĩa Factory để cung cấp Repository cho ViewModel này
    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

