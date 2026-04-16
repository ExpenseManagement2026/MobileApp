package com.example.mobileapp.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.repository.TransactionRepository
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

data class SpendingCategory(
    val name: String,
    val amount: Long,
    val colorHex: String,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val pieEntries: List<PieEntry> = emptyList(),
    val pieColors: List<Int> = emptyList(),
    val topCategories: List<SpendingCategory> = emptyList(),
    val allCategories: List<SpendingCategory> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
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

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        val (startDate, endDate) = getCurrentMonthRange()

        viewModelScope.launch {
            try {
                repository.getStatistics(startDate, endDate).collectLatest { stats ->
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
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        totalIncome = stats.totalIncome,
                        totalExpense = stats.totalExpense,
                        pieEntries = pieEntries,
                        pieColors = pieColors,
                        topCategories = coloredCategories.take(3),
                        allCategories = coloredCategories,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState(isLoading = false)
            }
        }

        viewModelScope.launch {
            try {
                repository.getAllTransactions().collectLatest { transactions ->
                    _uiState.value = _uiState.value.copy(recentTransactions = transactions.take(10))
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun getTransactionsByCategory(categoryName: String): List<Transaction> {
        return _uiState.value.recentTransactions.filter { it.category == categoryName }
    }

    fun getTransactionsByCategoryFlow(categoryName: String) = repository.getAllTransactions().map { transactions ->
        transactions.filter { it.category == categoryName }
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
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
