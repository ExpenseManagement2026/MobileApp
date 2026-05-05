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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        observeData()
    }

    fun previousMonth() {
        val newMonth = _selectedMonth.value.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _selectedMonth.value = newMonth
    }

    fun nextMonth() {
        val newMonth = _selectedMonth.value.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        // Không cho chọn tháng tương lai
        val now = Calendar.getInstance()
        if (newMonth.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
            (newMonth.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
             newMonth.get(Calendar.MONTH) <= now.get(Calendar.MONTH))) {
            _selectedMonth.value = newMonth
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            _selectedMonth.flatMapLatest { selectedCal ->
                val (startDate, endDate) = getMonthRange(selectedCal)
                val sdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
                val dateText = sdf.format(selectedCal.time)
                
                val month = selectedCal.get(Calendar.MONTH)
                val year = selectedCal.get(Calendar.YEAR)

                // Dùng combine thay vì nested collect
                combine(
                    dao.getTotalExpense(startDate, endDate),
                    dao.getCategoryStatistics("EXPENSE", startDate, endDate),
                ) { totalSpent, stats ->
                    val currentBudget = prefs.getBudget(month, year)
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

                    val remaining = useCase.getRemaining(currentBudget, totalSpent)
                    val remainingText = if (remaining < 0) {
                        useCase.formatCurrency(Math.abs(remaining))
                    } else {
                        useCase.formatCurrency(remaining)
                    }

                    BudgetState(
                        currentDateText = dateText,
                        budgetText = useCase.formatCurrency(currentBudget),
                        spentText = useCase.formatCurrency(totalSpent),
                        remainingText = remainingText,
                        percent = currentPercent,
                        statusColor = useCase.getStatusColor(currentPercent),
                        categories = categoryList,
                    )
                }
            }.collectLatest { newState ->
                _state.value = newState
            }
        }
    }

    fun saveNewBudget(amount: Long) {
        val selectedCal = _selectedMonth.value
        val month = selectedCal.get(Calendar.MONTH)
        val year = selectedCal.get(Calendar.YEAR)
        
        prefs.saveBudget(amount, month, year)
        _state.value = _state.value.copy(message = "Đã cập nhật ngân sách cho tháng ${month + 1}/${year}!")
        // Re-observe để cập nhật UI với budget mới
        observeData()
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun getMonthRange(cal: Calendar): Pair<Long, Long> {
        val startCal = cal.clone() as Calendar
        startCal.set(Calendar.DAY_OF_MONTH, 1)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        val start = startCal.timeInMillis

        val endCal = cal.clone() as Calendar
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        val end = endCal.timeInMillis

        return Pair(start, end)
    }
}
