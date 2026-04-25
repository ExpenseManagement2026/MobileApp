package com.example.mobileapp.domain.usecase.search

import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import java.util.Calendar

class FilterTransactionsUseCase {
    operator fun invoke(
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
            val matchesQuery = query.isBlank() ||
                    trans.title.contains(query, ignoreCase = true) ||
                    trans.note.contains(query, ignoreCase = true)

            val transCalendar = Calendar.getInstance().apply { timeInMillis = trans.date }
            val matchesFilter = when (filter) {
                FilterType.ALL -> true
                FilterType.TODAY -> transCalendar.get(Calendar.DAY_OF_YEAR) == currentDay && transCalendar.get(Calendar.YEAR) == currentYear
                FilterType.WEEK -> transCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && transCalendar.get(Calendar.YEAR) == currentYear
                FilterType.MONTH -> transCalendar.get(Calendar.MONTH) == currentMonth && transCalendar.get(Calendar.YEAR) == currentYear
                FilterType.INCOME -> trans.type == TransactionType.INCOME
                FilterType.EXPENSE -> trans.type == TransactionType.EXPENSE
                // Lọc bằng cách tìm từ khóa trong ghi chú
                FilterType.TRANSFER -> trans.note.contains("Chuyển khoản", ignoreCase = true)
                FilterType.CASH -> trans.note.contains("Tiền mặt", ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }
}
