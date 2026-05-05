package com.example.mobileapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.repository.TransactionRepository
import com.example.mobileapp.domain.usecase.search.FilterTransactionsUseCase
import com.example.mobileapp.domain.usecase.search.FilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CustomMonthFilter(val month: Int, val year: Int)  // month: 0-based

class SearchViewModel(
    private val repository: TransactionRepository,
    private val filterTransactionsUseCase: FilterTransactionsUseCase
) : ViewModel() {

    private val _searchQuery   = MutableStateFlow("")
    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    private val _customMonth   = MutableStateFlow<CustomMonthFilter?>(null)

    val searchQueryState:   StateFlow<String>                  = _searchQuery.asStateFlow()
    val currentFilterState: StateFlow<FilterType>              = _currentFilter.asStateFlow()
    val customMonthState:   StateFlow<CustomMonthFilter?>      = _customMonth.asStateFlow()

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<List<Transaction>> = combine(
        repository.getAllTransactions(),
        _searchQuery.debounce(300),
        _currentFilter,
        _customMonth,
    ) { transactions, query, filter, customMonth ->
        filterTransactionsUseCase(
            transactions = transactions,
            query = query,
            filter = filter,
            customMonth = customMonth?.month ?: -1,
            customYear  = customMonth?.year  ?: -1,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filterType: FilterType) {
        _currentFilter.value = filterType
        // Khi chọn filter khác, xóa custom month
        if (filterType != FilterType.CUSTOM_MONTH) {
            _customMonth.value = null
        }
    }

    fun setCustomMonth(month: Int, year: Int) {
        _customMonth.value = CustomMonthFilter(month, year)
        _currentFilter.value = FilterType.CUSTOM_MONTH
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    /** Danh sách các tháng có giao dịch để hiển thị trong picker */
    fun getAvailableMonths(transactions: List<Transaction>): List<CustomMonthFilter> {
        return transactions
            .map { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                CustomMonthFilter(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
            }
            .distinct()
            .sortedWith(compareByDescending<CustomMonthFilter> { it.year }.thenByDescending { it.month })
    }

    class Factory(
        private val repository: TransactionRepository,
        private val filterTransactionsUseCase: FilterTransactionsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository, filterTransactionsUseCase) as T
        }
    }
}
