package com.example.mobileapp.presentation.home

import androidx.lifecycle.ViewModel
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(buildMockState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private fun buildMockState(): HomeState {
        val transactions = listOf(
            Transaction("tx1", "🍜", "Ăn trưa", "Ăn uống", -85_000),
            Transaction("tx2", "🚕", "Grab về nhà", "Di chuyển", -45_000),
            Transaction("tx3", "🛒", "Siêu thị", "Mua sắm", -120_000),
            Transaction("tx4", "💊", "Thuốc", "Sức khỏe", -35_000),
        )
        val income = 15_200_000L
        val expense = transactions.sumOf { if (it.amount < 0) -it.amount else 0L }
        return HomeState(
            greeting = "Xin chào,",
            totalBalance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            chartData = listOf(100f, 180f, 220f, 310f, 290f, 340f, 380f, 420f),
            recentTransactions = transactions,
        )
    }
}
