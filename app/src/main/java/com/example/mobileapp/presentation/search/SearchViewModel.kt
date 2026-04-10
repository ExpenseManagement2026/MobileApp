package com.example.mobileapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

enum class FilterType {
    ALL, TODAY, WEEK, MONTH, INCOME, EXPENSE
}

class SearchViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // 1. Lưu trữ từ khóa tìm kiếm
    private val _searchQuery = MutableStateFlow("")

    // 2. Lưu trữ trạng thái bộ lọc (Mặc định là ALL)
    private val _currentFilter = MutableStateFlow(FilterType.ALL)

    // --- Expose công khai để Compose collectAsState() ---
    val searchQueryState: StateFlow<String> = _searchQuery.asStateFlow()
    val currentFilterState: StateFlow<FilterType> = _currentFilter.asStateFlow()

    // 3. Dòng chảy dữ liệu cuối cùng xuất ra cho Giao diện
    val uiState: StateFlow<List<Transaction>> = combine(
        repository.getAllTransactions(),
        _searchQuery,
        _currentFilter
    ) { transactions, query, filter ->
        filterTransactions(transactions, query, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Hàm cập nhật từ khóa khi user gõ phím
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Hàm cập nhật bộ lọc khi user bấm nút
    fun setFilter(filterType: FilterType) {
        _currentFilter.value = filterType
    }

    // --- LOGIC LỌC DỮ LIỆU ---
    private fun filterTransactions(
        transactions: List<Transaction>,
        query: String,
        filter: FilterType
    ): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return transactions.filter { trans ->
            // Lọc theo từ khóa (Tìm trong Title hoặc Note)
            val matchesQuery = query.isBlank() ||
                    trans.title.contains(query, ignoreCase = true) ||
                    trans.note.contains(query, ignoreCase = true)

            // Lọc theo thời gian / loại
            val transCalendar = Calendar.getInstance().apply { timeInMillis = trans.dateMillis }
            val matchesFilter = when (filter) {
                FilterType.ALL -> true
                FilterType.TODAY -> transCalendar.get(Calendar.DAY_OF_YEAR) == currentDay && transCalendar.get(Calendar.YEAR) == currentYear
                FilterType.WEEK -> transCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && transCalendar.get(Calendar.YEAR) == currentYear
                FilterType.MONTH -> transCalendar.get(Calendar.MONTH) == currentMonth && transCalendar.get(Calendar.YEAR) == currentYear
                FilterType.INCOME -> trans.type == "INCOME"
                FilterType.EXPENSE -> trans.type == "EXPENSE"
            }

            matchesQuery && matchesFilter
        }
    }

    // ── Factory (Manual DI — không dùng Hilt) ────────────────────────
    class Factory(
        private val repository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}