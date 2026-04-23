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
import java.util.*

enum class PaymentMethod(val label: String, val icon: String) {
    CASH("Tiền mặt", "💵"),
    TRANSFER("Chuyển khoản", "🏦")
}

data class AddTransactionState(
    val amount: String = "",
    val isExpense: Boolean = true,
    val selectedCategory: String = "Khác",
    val note: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isSaved: Boolean = false,
    val error: String? = null
)

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    fun setAmount(value: String) { 
        _state.value = _state.value.copy(amount = value, error = null) 
    }
    
    fun setType(isExpense: Boolean) { 
        _state.value = _state.value.copy(isExpense = isExpense) 
    }
    
    fun setCategory(name: String) { 
        _state.value = _state.value.copy(selectedCategory = name) 
    }
    
    fun setNote(text: String) { 
        _state.value = _state.value.copy(note = text) 
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _state.value = _state.value.copy(paymentMethod = method)
    }

    fun save() {
        val s = _state.value
        val amountValue = s.amount.replace(".", "").toLongOrNull() ?: 0L
        
        if (amountValue <= 0) {
            _state.value = s.copy(error = "Vui lòng nhập số tiền hợp lệ")
            return
        }

        viewModelScope.launch {
            val transaction = Transaction(
                title = s.selectedCategory,
                amount = amountValue,
                type = if (s.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                category = s.selectedCategory,
                date = System.currentTimeMillis(),
                note = if (s.note.isNotBlank()) "[${s.paymentMethod.label}] ${s.note}" else "[${s.paymentMethod.label}]"
            )
            repository.insertTransaction(transaction)
            _state.value = s.copy(isSaved = true)
        }
    }

    fun resetSaveState() {
        _state.value = _state.value.copy(isSaved = false, amount = "", note = "")
    }

    fun resetState() {
        _state.value = AddTransactionState()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = RepositoryProvider.provideTransactionRepository(application)
            return AddTransactionViewModel(repo) as T
        }
    }
}
