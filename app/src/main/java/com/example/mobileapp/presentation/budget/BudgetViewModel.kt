package com.example.mobileapp.presentation.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.local.BudgetPreferences
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CategoryBudget(
    val name: String,
    val spent: Long,
    val limit: Long,
    val percent: Int,
    val status: String,
    val color: String
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BudgetPreferences(application)
    private val useCase = CheckBudgetUseCase()
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    private val _budgetText = MutableLiveData<String>()
    val budgetText: LiveData<String> = _budgetText

    private val _percent = MutableLiveData<Int>()
    val percent: LiveData<Int> = _percent

    private val _spentText = MutableLiveData<String>()
    val spentText: LiveData<String> = _spentText

    private val _remainingText = MutableLiveData<String>()
    val remainingText: LiveData<String> = _remainingText

    private val _statusColor = MutableLiveData<String>()
    val statusColor: LiveData<String> = _statusColor

    private val _categories = MutableLiveData<List<CategoryBudget>>()
    val categories: LiveData<List<CategoryBudget>> = _categories

    // THÊM BIẾN THÁNG HIỆN TẠI
    private val _currentDateText = MutableLiveData<String>()
    val currentDateText: LiveData<String> = _currentDateText

    // THÊM BIẾN THÔNG BÁO
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun refreshBudgetData() {
        val calendar = Calendar.getInstance()
        
        // Cập nhật text hiển thị tháng: "Tháng 04/2024"
        val sdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
        _currentDateText.value = sdf.format(calendar.time)

        viewModelScope.launch {
            val currentBudget = prefs.getBudget()
            
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = calendar.timeInMillis

            // Lấy dữ liệu từ Database
            transactionDao.getTotalExpense(startDate, endDate).collect { totalSpent ->
                _budgetText.value = useCase.formatCurrency(currentBudget)
                val currentPercent = useCase.getUsedPercent(totalSpent, currentBudget)
                _percent.value = currentPercent
                _spentText.value = useCase.formatCurrency(totalSpent)
                _remainingText.value = useCase.formatCurrency(useCase.getRemaining(currentBudget, totalSpent))
                _statusColor.value = useCase.getStatusColor(currentPercent)

                transactionDao.getCategoryStatistics("EXPENSE", startDate, endDate).collect { stats ->
                    val categoryList = stats.map { stat ->
                        val categoryLimit = if (stats.isNotEmpty()) currentBudget / stats.size else currentBudget
                        val p = useCase.getUsedPercent(stat.totalAmount, categoryLimit)
                        CategoryBudget(
                            name = stat.category,
                            spent = stat.totalAmount,
                            limit = categoryLimit,
                            percent = p,
                            status = useCase.getStatus(p),
                            color = useCase.getStatusColor(p)
                        )
                    }
                    _categories.value = categoryList
                }
            }
        }
    }

    fun saveNewBudget(amount: Long) {
        prefs.saveBudget(amount)
        _message.value = "Đã cập nhật ngân sách thành công!"
        refreshBudgetData()
    }

    fun clearMessage() {
        _message.value = null
    }
}
