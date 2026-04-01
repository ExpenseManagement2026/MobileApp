package com.example.mobileapp.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobileapp.data.local.BudgetPreferences
import com.example.mobileapp.domain.usecase.CheckBudgetUseCase

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    // data local
    private val prefs = BudgetPreferences(application)

    // logic
    private val useCase = CheckBudgetUseCase()

    // ===== STATE =====

    private val _budget = MutableLiveData<Long>()
    val budget: LiveData<Long> = _budget

    private val _percent = MutableLiveData<Int>()
    val percent: LiveData<Int> = _percent

    private val _remaining = MutableLiveData<Long>()
    val remaining: LiveData<Long> = _remaining

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    // 📥 Input:
    // - totalSpent (tạm thời giả)
    // 📤 Output:
    // - cập nhật toàn bộ UI data
    fun loadBudget(totalSpent: Long) {

        val currentBudget = prefs.getBudget()

        _budget.value = currentBudget

        val percent = useCase.getUsedPercent(totalSpent, currentBudget)
        _percent.value = percent

        _remaining.value = useCase.getRemaining(currentBudget, totalSpent)

        _status.value = useCase.getStatus(percent)
    }

    // 📥 Input:
    // - amount (budget mới)
    // - totalSpent
    // 📤 Output:
    // - cập nhật lại UI
    fun saveBudget(amount: Long, totalSpent: Long) {

        prefs.saveBudget(amount) // lưu

        loadBudget(totalSpent) // load lại
    }
}