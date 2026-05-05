package com.example.mobileapp.data.local

import android.content.Context
import java.util.Calendar

class BudgetPreferences(context: Context) {

    // SharedPreferences để lưu dữ liệu local
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    companion object {
        // Key prefix dùng để lưu budget theo tháng
        private const val KEY_BUDGET_PREFIX = "key_budget_"
    }

    /**
     * Lưu ngân sách cho một tháng cụ thể
     * @param amount số tiền ngân sách
     * @param month tháng (0-11, Calendar.MONTH)
     * @param year năm
     */
    fun saveBudget(amount: Long, month: Int, year: Int) {
        val key = "${KEY_BUDGET_PREFIX}${year}_${month}"
        prefs.edit()
            .putLong(key, amount)
            .apply()
    }

    /**
     * Lấy ngân sách của một tháng cụ thể
     * @param month tháng (0-11, Calendar.MONTH)
     * @param year năm
     * @return budget đã lưu, nếu chưa có trả về 0
     */
    fun getBudget(month: Int, year: Int): Long {
        val key = "${KEY_BUDGET_PREFIX}${year}_${month}"
        return prefs.getLong(key, 0L)
    }

    /**
     * Lưu ngân sách cho tháng hiện tại (backward compatibility)
     */
    fun saveBudget(amount: Long) {
        val cal = Calendar.getInstance()
        saveBudget(amount, cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }

    /**
     * Lấy ngân sách tháng hiện tại (backward compatibility)
     */
    fun getBudget(): Long {
        val cal = Calendar.getInstance()
        return getBudget(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }
}