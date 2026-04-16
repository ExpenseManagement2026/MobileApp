package com.example.mobileapp.presentation.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import com.example.mobileapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddTransactionState(
    val isExpense: Boolean = true,
    val amount: String = "",
    val selectedCategory: String = "",
    val note: String = "",
    val isSaved: Boolean = false,
    val error: String? = null,
)

val expenseCategories = listOf(
    "Ăn uống" to "🍜",
    "Di chuyển" to "🚕",
    "Mua sắm" to "🛒",
    "Hóa đơn" to "⚡",
    "Giải trí" to "🎮",
    "Sức khỏe" to "💊",
    "Giáo dục" to "📚",
    "Khác" to "📦",
)

val incomeCategories = listOf(
    "Lương" to "💰",
    "Thưởng" to "🎁",
    "Đầu tư" to "📈",
    "Freelance" to "💻",
    "Khác" to "📦",
)

class AddTransactionViewModel(
    application: Application,
    private val repository: TransactionRepository,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    fun setType(isExpense: Boolean) {
        _state.value = _state.value.copy(isExpense = isExpense, selectedCategory = "")
    }

    fun setAmount(amount: String) {
        _state.value = _state.value.copy(amount = amount, error = null)
    }

    fun setAmountFromScan(amount: Double) {
        _state.value = _state.value.copy(amount = amount.toLong().toString(), error = null)
    }

    fun setCategory(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun setNote(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun save() {
        val s = _state.value
        val amountLong = s.amount.replace(".", "").toLongOrNull()
        when {
            amountLong == null || amountLong <= 0 ->
                _state.value = s.copy(error = "Vui lòng nhập số tiền hợp lệ")
            s.selectedCategory.isEmpty() ->
                _state.value = s.copy(error = "Vui lòng chọn danh mục")
            else -> viewModelScope.launch {
                repository.insertTransaction(
                    Transaction(
                        title = s.selectedCategory,
                        amount = amountLong,
                        type = if (s.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                        category = s.selectedCategory,
                        date = Calendar.getInstance().timeInMillis,
                        note = s.note,
                    )
                )
                // Chỉ set isSaved = true, giữ nguyên các field khác
                _state.value = s.copy(isSaved = true)
            }
        }
    }

    fun resetState() {
        _state.value = AddTransactionState()
    }

    fun markSavedHandled() {
        _state.value = _state.value.copy(isSaved = false)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val repo = RepositoryProvider.provideTransactionRepository(application)
            return AddTransactionViewModel(application, repo) as T
        }
    }
}
