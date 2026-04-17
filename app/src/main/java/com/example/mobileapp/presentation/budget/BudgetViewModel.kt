package com.example.mobileapp.presentation.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.local.BudgetPreferences
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.data.mapper.toDomainList
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models ---
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
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.transactionDao()

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val now = Calendar.getInstance()
        val sdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
        val dateText = sdf.format(now.time)

        viewModelScope.launch {
            val (start, end) = currentMonthRange()
            combine(
                dao.getTotalExpense(start, end),
                dao.getCategoryStatistics("EXPENSE", start, end)
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

    /** Lấy danh sách giao dịch chi tiết cho Dialog */
    fun getTransactionsByCategory(categoryName: String): Flow<List<Transaction>> {
        val (start, end) = currentMonthRange()
        return dao.getTransactionsByCategory(categoryName).map { list ->
            list.toDomainList().filter { it.date in start..end }
        }
    }

    fun saveNewBudget(amount: Long) {
        prefs.saveBudget(amount)
        _state.value = _state.value.copy(message = "Đã cập nhật ngân sách thành công!")
        observeData()
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        return Pair(start, cal.timeInMillis)
    }
}
