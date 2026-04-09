package com.example.mobileapp.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =============================================
// DATA MODEL - Đại diện cho 1 hạng mục chi tiêu
// =============================================
data class SpendingCategory(
    val name: String,       // Tên hạng mục
    val amount: Long,       // Số tiền (VND)
    val colorHex: String    // Màu hiển thị (hex string)
)

// =============================================
// DATA MODEL - Đại diện cho 1 giao dịch chi tiêu
// =============================================
data class Transaction(
    val id: String,
    val categoryName: String,
    val description: String,
    val amount: Long,
    val date: String  // Format: "dd/MM/yyyy"
)

// =============================================
// UI STATE - Trạng thái toàn bộ màn hình Dashboard
// Đây là "single source of truth" mà Fragment sẽ observe
// =============================================
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalIncome: Long = 0L,         // Tổng thu
    val totalExpense: Long = 0L,        // Tổng chi
    val pieEntries: List<PieEntry> = emptyList(),           // Dữ liệu cho PieChart
    val pieColors: List<Int> = emptyList(),                 // Màu tương ứng từng slice
    val topCategories: List<SpendingCategory> = emptyList() // Top 3 hạng mục chi nhiều nhất
)

// =============================================
// VIEWMODEL - Tầng xử lý logic UI (MVVM Layer)
// Không giữ reference đến View/Fragment
// =============================================
class DashboardViewModel : ViewModel() {

    // StateFlow là "luồng dữ liệu" một chiều: ViewModel -> UI
    // _uiState là private (chỉ ViewModel được ghi)
    private val _uiState = MutableStateFlow(DashboardUiState())

    // uiState là public (Fragment chỉ được đọc)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Lưu trữ toàn bộ lịch sử giao dịch (dummy data)
    private val allTransactions = listOf(
        Transaction("1", "Ăn uống", "Cơm trưa quán Phở", 45_000L, "08/04/2024"),
        Transaction("2", "Ăn uống", "Cafe sáng", 35_000L, "08/04/2024"),
        Transaction("3", "Di chuyển", "Grab đi làm", 42_000L, "08/04/2024"),
        Transaction("4", "Ăn uống", "Ăn tối nhà hàng", 320_000L, "07/04/2024"),
        Transaction("5", "Mua sắm", "Áo thun Uniqlo", 299_000L, "07/04/2024"),
        Transaction("6", "Di chuyển", "Xăng xe", 150_000L, "06/04/2024"),
        Transaction("7", "Giải trí", "Vé xem phim", 120_000L, "06/04/2024"),
        Transaction("8", "Ăn uống", "Lẩu buffet", 450_000L, "05/04/2024"),
        Transaction("9", "Mua sắm", "Giày thể thao", 890_000L, "04/04/2024"),
        Transaction("10", "Giải trí", "Karaoke", 200_000L, "03/04/2024")
    )

    init {
        // Tự động load dữ liệu khi ViewModel được khởi tạo
        loadDashboardData()
    }

    /**
     * Lấy lịch sử giao dịch theo danh mục
     */
    fun getTransactionsByCategory(categoryName: String): List<Transaction> {
        return allTransactions.filter { it.categoryName == categoryName }
    }

    /**
     * Giả lập việc load dữ liệu từ Repository (tầng Data).
     * Trong thực tế, hàm này sẽ gọi repository.getDashboardData()
     * và xử lý kết quả trả về (Success/Error).
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            // Bật trạng thái loading
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Giả lập độ trễ mạng (500ms)
            delay(500)

            // ---- DUMMY DATA (sẽ thay bằng dữ liệu thật từ DB/API) ----
            val categories = listOf(
                SpendingCategory("Ăn uống",   3_250_000L, "#EF5350"),
                SpendingCategory("Di chuyển", 2_180_000L, "#5C6BC0"),
                SpendingCategory("Mua sắm",   1_850_000L, "#FFA726"),
                SpendingCategory("Giải trí",    980_000L, "#26A69A"),
                SpendingCategory("Khác",        660_000L, "#AB47BC")
            )

            val totalExpense = categories.sumOf { it.amount }
            val totalIncome  = 15_000_000L

            // Chuyển đổi sang PieEntry cho MPAndroidChart
            // PieEntry(value, label) - value là phần trăm
            val pieEntries = categories.map { cat ->
                PieEntry(
                    (cat.amount.toFloat() / totalExpense.toFloat()) * 100f,
                    cat.name
                )
            }

            // Chuyển màu hex sang Int cho MPAndroidChart
            val pieColors = categories.map {
                android.graphics.Color.parseColor(it.colorHex)
            }

            // Cập nhật UI State - Fragment sẽ tự động nhận được thay đổi này
            _uiState.value = DashboardUiState(
                isLoading      = false,
                totalIncome    = totalIncome,
                totalExpense   = totalExpense,
                pieEntries     = pieEntries,
                pieColors      = pieColors,
                topCategories  = categories.sortedByDescending { it.amount }.take(3)
            )
        }
    }
}
