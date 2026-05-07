package com.example.mobileapp.domain.usecase.search

import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import java.util.Calendar

class FilterTransactionsUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        query: String,
        filter: FilterType,
        customMonth: Int = -1,   // 0-based Calendar.MONTH
        customYear: Int = -1,
    ): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentDay   = calendar.get(Calendar.DAY_OF_YEAR)
        val currentWeek  = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear  = calendar.get(Calendar.YEAR)

        return transactions.filter { trans ->
            val matchesQuery = query.isBlank() ||
                    trans.title.contains(query, ignoreCase = true) ||
                    trans.note.contains(query, ignoreCase = true)

            val txCal = Calendar.getInstance().apply { timeInMillis = trans.date }
            val matchesFilter = when (filter) {
                FilterType.ALL     -> true
                FilterType.TODAY   -> txCal.get(Calendar.DAY_OF_YEAR) == currentDay &&
                                      txCal.get(Calendar.YEAR) == currentYear
                FilterType.WEEK    -> txCal.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                                      txCal.get(Calendar.YEAR) == currentYear
                FilterType.MONTH   -> txCal.get(Calendar.MONTH) == currentMonth &&
                                      txCal.get(Calendar.YEAR) == currentYear
                FilterType.INCOME  -> trans.type == TransactionType.INCOME
                FilterType.EXPENSE -> trans.type == TransactionType.EXPENSE
                FilterType.CUSTOM_MONTH -> customMonth >= 0 && customYear >= 0 &&
                                      txCal.get(Calendar.MONTH) == customMonth &&
                                      txCal.get(Calendar.YEAR) == customYear
            }

            matchesQuery && matchesFilter
        }
    }
}
