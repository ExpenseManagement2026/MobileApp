package com.example.mobileapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.Transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HomeViewModel đóng vai trò là "bộ não" quản lý dữ liệu cho màn hình HomeScreen.
 * Nó kế thừa từ ViewModel để giữ lại dữ liệu ngay cả khi màn hình bị xoay.
 */
class HomeViewModel : ViewModel() {

    // _state là biến nội bộ dùng để cập nhật dữ liệu (MutableStateFlow)
    private val _state = MutableStateFlow(HomeState())
    
    // state là biến công khai chỉ cho phép đọc để UI quan sát (StateFlow)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Khối init sẽ chạy ngay khi ViewModel được khởi tạo
    init {
        getTransactions()
    }

    /**
     * Hàm lấy danh sách giao dịch từ nguồn dữ liệu (Database hoặc API).
     */
    fun getTransactions() {
        // viewModelScope giúp chạy tác vụ này ở background, không làm treo máy
        viewModelScope.launch {
            
            // Bước 1: Bật trạng thái "Đang tải" (isLoading = true) để UI hiện vòng xoay loading
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Giả lập thời gian chờ load dữ liệu từ Database (ví dụ 1 giây)
                delay(1000) 

                // Bước 2: Chuẩn bị dữ liệu thực tế (Sau này thay chỗ này bằng cách gọi Repository)
                val realDataFromSource = listOf(
                    Transaction("1", "🍜", "Ăn trưa", "Ăn uống", -85000),
                    Transaction("2", "🚕", "Grab về nhà", "Di chuyển", -45000),
                    Transaction("3", "🛒", "Siêu thị", "Mua sắm", -120000),
                    Transaction("4", "💊", "Thuốc", "Sức khỏe", -35000),
                    Transaction("5", "💰", "Lương tháng", "Thu nhập", 15200000),
                    Transaction("6", "☕", "Cà phê", "Giải trí", -30000),
                    Transaction("7", "⚡", "Tiền điện", "Hóa đơn", -450000)
                )

                // Bước 3: Logic tính toán các con số tổng quát
                // - Lọc các giao dịch dương để tính tổng Thu nhập
                val income = realDataFromSource.filter { it.amount > 0 }.sumOf { it.amount }
                
                // - Lọc các giao dịch âm để tính tổng Chi tiêu (đổi dấu sang dương để hiện số)
                val expense = realDataFromSource.filter { it.amount < 0 }.sumOf { -it.amount }
                
                // - Số dư = Thu nhập - Chi tiêu
                val balance = income - expense

                // Bước 4: Cập nhật toàn bộ kết quả vào State để UI tự động vẽ lại
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false, // Tắt loading
                        totalBalance = balance,
                        totalIncome = income,
                        totalExpense = expense,
                        recentTransactions = realDataFromSource.sortedByDescending { it.id }, // Sắp xếp cái mới nhất lên đầu
                        chartData = listOf(100f, 150f, 200f, 350f, 250f, 300f, 420f) // Dữ liệu cho biểu đồ
                    )
                }

            } catch (e: Exception) {
                // Bước 5: Nếu có lỗi (mất mạng, lỗi DB), thông báo lỗi cho người dùng
                _state.update { it.copy(isLoading = false, error = "Lỗi tải dữ liệu: ${e.message}") }
            }
        }
    }

    /**
     * Hàm dùng để làm mới dữ liệu khi người dùng kéo màn hình xuống
     */
    fun refresh() {
        getTransactions()
    }
}
