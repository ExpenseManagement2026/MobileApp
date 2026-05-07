package com.example.mobileapp.data.local

import android.content.Context
import java.util.Calendar

class BudgetPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BUDGET_PREFIX = "key_budget_"
        private const val KEY_BUDGET_OLD = "key_budget" // Key cũ của bạn
    }

    fun saveBudget(amount: Long, month: Int, year: Int) {
        val key = "${KEY_BUDGET_PREFIX}${year}_${month}"
        prefs.edit()
            .putLong(key, amount)
            .apply()
    }

    fun getBudget(month: Int, year: Int): Long {
        val key = "${KEY_BUDGET_PREFIX}${year}_${month}"
        val monthBudget = prefs.getLong(key, 0L)
        
        // DỰ PHÒNG: Nếu tháng này chưa có ngân sách, lấy ngân sách chung (key cũ)
        return if (monthBudget > 0L) monthBudget else prefs.getLong(KEY_BUDGET_OLD, 0L)
    }

    fun saveBudget(amount: Long) {
        // Lưu cho cả key tháng và key cũ để đảm bảo tính tương thích
        val cal = Calendar.getInstance()
        saveBudget(amount, cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
        prefs.edit().putLong(KEY_BUDGET_OLD, amount).apply()
    }

    fun getBudget(): Long {
        val cal = Calendar.getInstance()
        return getBudget(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }
}
