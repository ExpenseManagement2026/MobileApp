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

class SearchViewModel(
    private val repository: TransactionRepository,
    private val filterTransactionsUseCase: FilterTransactionsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _currentFilter = MutableStateFlow(FilterType.ALL)

    val searchQueryState: StateFlow<String> = _searchQuery.asStateFlow()
    val currentFilterState: StateFlow<FilterType> = _currentFilter.asStateFlow()

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<List<Transaction>> = combine(
        repository.getAllTransactions(),
        _searchQuery.debounce(300), // Chỉ lọc khi người dùng ngừng gõ 300ms
        _currentFilter
    ) { transactions, query, filter ->
        filterTransactionsUseCase(transactions, query, filter)
    }
        .flowOn(Dispatchers.Default) // Chạy logic lọc nặng trên luồng nền
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