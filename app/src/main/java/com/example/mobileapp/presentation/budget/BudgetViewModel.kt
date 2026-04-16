package com.example.mobileapp.presentation.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.local.BudgetPreferences
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CategoryBudget(
    val name: String,
    val spent: Long,
    val limit: Long,
    val percent: Int,
    val status: String,
    val color: String,
)

data class BudgetState(
    val currentDateText: String = "",
    val budgetText: String = "0 đ",
    val spentText: String = "0 đ",
    val remainingText: String = "0 đ",
    val percent: Int = 0,
    val statusColor: String = "#2DC98E",
    val categories: List<CategoryBudget> = emptyList(),
    val message: String? = null,
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BudgetPreferences(application)
    private val useCase = CheckBudgetUseCase()
    private val dao = AppDatabase.getDatabase(application).transactionDao()

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val (startDate, endDate) = currentMonthRange()
        val sdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
        val dateText = sdf.format(Calendar.getInstance().time)

        viewModelScope.launch {
            // Dùng combine thay vì nested collect
            combine(
                dao.getTotalExpense(startDate, endDate),
                dao.getCategoryStatistics("EXPENSE", startDate, endDate),
            ) { totalSpent, stats ->
                val currentBudget = prefs.getBudget()
                val currentPercent = useCase.getUsedPercent(totalSpent, currentBudget)

                val categoryList = stats.map { stat ->
                    val categoryLimit = if (stats.isNotEmpty()) currentBudget / stats.size else currentBudget
                    val p = useCase.getUsedPercent(stat.totalAmount, categoryLimit)
                    CategoryBudget(
                        name = stat.category,
                        spent = stat.totalAmount,
                        limit = categoryLimit,
                        percent = p,
                        status = useCase.getStatus(p),
                        color = useCase.getStatusColor(p),
                    )
                }

                BudgetState(
                    currentDateText = dateText,
                    budgetText = useCase.formatCurrency(currentBudget),
                    spentText = useCase.formatCurrency(totalSpent),
                    remainingText = useCase.formatCurrency(useCase.getRemaining(currentBudget, totalSpent)),
                    percent = currentPercent,
                    statusColor = useCase.getStatusColor(currentPercent),
                    categories = categoryList,
                )
            }.collectLatest { newState ->
                _state.value = newState
            }
        }
    }

    fun saveNewBudget(amount: Long) {
        prefs.saveBudget(amount)
        _state.value = _state.value.copy(message = "Đã cập nhật ngân sách thành công!")
        // Re-observe để cập nhật UI với budget mới
        observeData()
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
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
}
