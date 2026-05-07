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
    val transactionId: Long? = null,  // null = Add mode, not null = Edit mode
    val amount: String = "",
    val isExpense: Boolean = true,
    val selectedCategory: String = "Khác",
    val note: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val selectedDate: Long = Calendar.getInstance().timeInMillis,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false,
)

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val transaction = repository.getTransactionById(transactionId)
            if (transaction != null) {
                // Try to parse payment method from note for existing transactions
                val method = if (transaction.note.contains("[Tiền mặt]")) PaymentMethod.CASH 
                             else if (transaction.note.contains("[Chuyển khoản]")) PaymentMethod.TRANSFER 
                             else PaymentMethod.CASH
                
                val displayNote = transaction.note
                    .replace("[Tiền mặt]", "")
                    .replace("[Chuyển khoản]", "")
                    .trim()

                _state.value = AddTransactionState(
                    transactionId = transaction.id,
                    isExpense = transaction.type == TransactionType.EXPENSE,
                    amount = transaction.amount.toString(),
                    selectedCategory = transaction.category,
                    note = displayNote,
                    paymentMethod = method,
                    selectedDate = transaction.date,
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Không tìm thấy giao dịch")
            }
        }
    }

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

    fun setDate(timestamp: Long) {
        _state.value = _state.value.copy(selectedDate = timestamp)
    }

    fun save() {
        val s = _state.value
        val amountLong = s.amount.replace(".", "").toLongOrNull() ?: 0L
        
        if (amountLong <= 0) {
            _state.value = s.copy(error = "Vui lòng nhập số tiền hợp lệ")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true)
            val formattedNote = if (s.note.isNotBlank()) "[${s.paymentMethod.label}] ${s.note}" else "[${s.paymentMethod.label}]"
            
            val transaction = Transaction(
                id = s.transactionId ?: 0L,
                title = s.selectedCategory,
                amount = amountLong,
                type = if (s.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                category = s.selectedCategory,
                date = s.selectedDate,
                note = formattedNote
            )

            if (s.transactionId != null) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
            _state.value = s.copy(isSaved = true, isLoading = false)
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
