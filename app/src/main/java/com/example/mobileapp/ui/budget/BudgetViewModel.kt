package com.example.mobileapp.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobileapp.data.local.BudgetPreferences
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase

// Data class để hiển thị từng dòng danh mục
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

    // DANH SÁCH DANH MỤC
    private val _categories = MutableLiveData<List<CategoryBudget>>()
    val categories: LiveData<List<CategoryBudget>> = _categories

    fun refreshBudgetData(totalSpent: Long) {
        val currentBudget = prefs.getBudget()
        val currentPercent = useCase.getUsedPercent(totalSpent, currentBudget)

        _budgetText.value = useCase.formatCurrency(currentBudget)
        _percent.value = currentPercent
        _spentText.value = useCase.formatCurrency(totalSpent)
        _remainingText.value = useCase.formatCurrency(useCase.getRemaining(currentBudget, totalSpent))
        _statusColor.value = useCase.getStatusColor(currentPercent)

        // TẠO DỮ LIỆU GIẢ CHO DANH MỤC (Sau này sẽ lấy từ Database thật)
        _categories.value = listOf(
            CategoryBudget("Ăn uống", 2160000L, 3000000L, 72, "Cảnh báo", "#FFA000"),
            CategoryBudget("Di chuyển", 900000L, 2000000L, 45, "An toàn", "#00BFA5")
        )
    }

    fun saveNewBudget(amount: Long, totalSpent: Long) {
        prefs.saveBudget(amount)
        refreshBudgetData(totalSpent)
    }
}
