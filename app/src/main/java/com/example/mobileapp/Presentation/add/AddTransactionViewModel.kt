package com.example.mobileapp.presentation.add

import android.app.Application
import androidx.lifecycle.ViewModel
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
import java.util.Date

data class AddTransactionState(
    val amount: String = "",
    val isExpense: Boolean = true,
    val selectedCategory: String = "Khác",
    val note: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    fun setAmount(value: String) { _state.value = _state.value.copy(amount = value) }
    fun setType(isExpense: Boolean) { _state.value = _state.value.copy(isExpense = isExpense) }
    fun setCategory(name: String) { _state.value = _state.value.copy(selectedCategory = name) }
    fun setNote(text: String) { _state.value = _state.value.copy(note = text) }

    fun save() {
        val amountValue = _state.value.amount.toLongOrNull() ?: 0L
        if (amountValue <= 0) {
            _state.value = _state.value.copy(error = "Vui lòng nhập số tiền hợp lệ")
            return
        }

        viewModelScope.launch {
            val transaction = Transaction(
                title = _state.value.selectedCategory,
                amount = amountValue,
                type = if (_state.value.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                category = _state.value.selectedCategory,
                date = Date().time,
                note = _state.value.note
            )
            repository.insertTransaction(transaction)
            _state.value = _state.value.copy(isSaved = true)
        }
    }

    // QUAN TRỌNG: Hàm này để xóa trạng thái đã lưu, giúp không bị nhảy tab liên tục
    fun resetSaveState() {
        _state.value = _state.value.copy(isSaved = false, amount = "", note = "")
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = RepositoryProvider.provideTransactionRepository(application)
            return AddTransactionViewModel(repo) as T
        }
    }
}
