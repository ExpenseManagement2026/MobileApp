package com.example.mobileapp.data.local

import android.content.Context

class BudgetPreferences(context: Context) {

    // SharedPreferences để lưu dữ liệu local
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    companion object {
        // Key dùng để lưu budget
        private const val KEY_BUDGET = "key_budget"
    }

    // Input: amount (số tiền)
    // Output: không có (chỉ lưu)
    fun saveBudget(amount: Long) {
        prefs.edit()
            .putLong(KEY_BUDGET, amount) // lưu giá trị
            .apply() // apply = lưu async (nhanh hơn commit)
    }

    // Input: không có
    // Output: budget đã lưu (Long)
    fun getBudget(): Long {
        return prefs.getLong(KEY_BUDGET, 0L) // nếu chưa có → trả về 0
    }
}