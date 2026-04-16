package com.example.mobileapp.presentation.dashboard

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.mobileapp.domain.model.Transaction
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import java.text.NumberFormat
import java.util.Locale

// =============================================
// HELPER - Format số tiền sang dạng "8.920.000 đ"
// =============================================
private fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} đ"
}

// =============================================
// ROOT COMPOSABLE - Điểm vào của màn hình Dashboard
// Kết nối ViewModel với UI (tương đương Fragment trong View system)
// =============================================
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    // ---- OBSERVE STATE ----
    // collectAsState() chuyển StateFlow thành State<T> của Compose
    // Mỗi khi uiState thay đổi, Compose tự động recompose lại UI
    val uiState by viewModel.uiState.collectAsState()

    // State để quản lý dialog hiển thị lịch sử
    var selectedCategory by remember { mutableStateOf<SpendingCategory?>(null) }

    val greenColor  = Color(0xFF26A480)
    val headerGreen = Color(0xFF2ECC9A)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {

        // =============================================
        // HEADER - Tổng chi tiêu tháng (nền xanh)
        // =============================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(greenColor)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = uiState.currentMonth.ifEmpty { "Tháng 4/2024" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = headerGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tổng chi tiêu tháng này", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            }
                            uiState.error != null -> {
                                Text(
                                    text = "Lỗi: ${uiState.error}",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = formatCurrency(uiState.totalExpense),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Số dư: ${formatCurrency(uiState.balance)}",
                                    color = if (uiState.balance >= 0) Color.White else Color(0xFFFFCDD2),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================
        // CARD ROW - Tổng Thu & Tổng Chi
        // =============================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tổng Thu (xanh)
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Tổng Thu",
                amount = uiState.totalIncome,
                isLoading = uiState.isLoading,
                containerColor = Color(0xFF26A480),
                textColor = Color.White
            )
            // Tổng Chi (đỏ)
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Tổng Chi",
                amount = uiState.totalExpense,
                isLoading = uiState.isLoading,
                containerColor = Color(0xFFEF5350),
                textColor = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================
        // CARD - PieChart chi tiêu theo danh mục
        // =============================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Chi tiêu theo danh mục",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = greenColor)
                    }
                } else {
                    // AndroidView cho phép nhúng View truyền thống (MPAndroidChart)
                    // vào trong Compose - đây là cầu nối giữa 2 hệ thống UI
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        factory = { context ->
                            // factory chỉ chạy 1 lần khi tạo View
                            PieChart(context).apply {
                                configurePieChart(this, uiState)
                            }
                        },
                        update = { chart ->
                            // update chạy mỗi khi state thay đổi (recompose)
                            configurePieChart(chart, uiState)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Legend - Chú thích màu cho từng danh mục
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.allCategories.chunked(2).forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    LegendItem(
                                        category = category,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Fill empty space nếu số lẻ
                                if (rowCategories.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================
        // CARD - Top 3 hạng mục chi tiêu nhiều nhất
        // =============================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Top chi tiêu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEEEEEE))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    // Hiển thị top 3 hạng mục
                    uiState.topCategories.forEachIndexed { index, category ->
                        CategoryRow(
                            category = category,
                            totalExpense = uiState.totalExpense,
                            onClick = { selectedCategory = category }
                        )
                        if (index < uiState.topCategories.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFF0F0F0)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================
        // CARD - Toàn bộ danh mục chi tiêu
        // =============================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tất cả danh mục",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEEEEEE))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    // Hiển thị toàn bộ danh mục
                    uiState.allCategories.forEachIndexed { index, category ->
                        CategoryRow(
                            category = category,
                            totalExpense = uiState.totalExpense,
                            onClick = { selectedCategory = category }
                        )
                        if (index < uiState.allCategories.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFF0F0F0)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // =============================================
    // DIALOG - Hiển thị lịch sử chi tiêu khi click vào danh mục
    // =============================================
    selectedCategory?.let { category ->
        TransactionHistoryDialog(
            category = category,
            transactions = viewModel.getTransactionsByCategory(category.name),
            onDismiss = { selectedCategory = null }
        )
    }
}

// =============================================
// COMPOSABLE - Card hiển thị Tổng Thu / Tổng Chi
// =============================================
@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Long,
    isLoading: Boolean,
    containerColor: Color,
    textColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = label, color = textColor.copy(alpha = 0.85f), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            if (isLoading) {
                CircularProgressIndicator(color = textColor, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = formatCurrency(amount),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// =============================================
// COMPOSABLE - 1 dòng trong danh sách Top 3
// =============================================
@Composable
private fun CategoryRow(
    category: SpendingCategory,
    totalExpense: Long,
    onClick: () -> Unit
) {
    val dotColor = Color(AndroidColor.parseColor(category.colorHex))
    val percent = if (totalExpense > 0) (category.amount * 100f / totalExpense) else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chấm màu
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        // Tên hạng mục
        Text(
            text = category.name,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = Color(0xFF424242)
        )
        // Phần trăm
        Text(
            text = "%.1f%%".format(percent),
            fontSize = 13.sp,
            color = Color(0xFF9E9E9E)
        )
        Spacer(modifier = Modifier.width(12.dp))
        // Số tiền
        Text(
            text = formatCurrency(category.amount),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF212121)
        )
    }
}

// =============================================
// COMPOSABLE - Legend item (chú thích màu)
// =============================================
@Composable
private fun LegendItem(
    category: SpendingCategory,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Hình vuông màu
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = Color(AndroidColor.parseColor(category.colorHex)),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        // Tên danh mục
        Text(
            text = category.name,
            fontSize = 12.sp,
            color = Color(0xFF616161),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

// =============================================
// HELPER - Cấu hình PieChart (MPAndroidChart)
// Tách ra hàm riêng để dùng lại ở factory và update
// =============================================
private fun configurePieChart(chart: PieChart, state: DashboardUiState) {
    if (state.pieEntries.isEmpty()) return

    val dataSet = PieDataSet(state.pieEntries, "").apply {
        colors = state.pieColors
        sliceSpace = 3f          // Khoảng cách giữa các slice
        selectionShift = 6f      // Độ nổi khi chọn
        valueTextSize = 0f       // Ẩn % trên slice
        setDrawValues(false)     // Ẩn hoàn toàn text trên slice
    }

    chart.apply {
        data = PieData(dataSet)

        // Cấu hình dạng Donut (hình tròn rỗng giữa)
        isDrawHoleEnabled = true
        holeRadius = 58f
        transparentCircleRadius = 62f
        setHoleColor(AndroidColor.WHITE)

        // Text ở giữa vòng tròn
        centerText = "${formatCurrency(state.totalExpense)}\nTổng chi tiêu"
        setCenterTextSize(14f)
        setCenterTextColor(AndroidColor.parseColor("#212121"))
        setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)

        // Tắt legend mặc định (dùng Compose legend bên dưới)
        legend.isEnabled = false
        description.isEnabled = false
        
        // Tắt entry labels (tên category trên slice)
        setDrawEntryLabels(false)

        // Hiệu ứng quay khi load
        animateY(1000, Easing.EaseInOutQuad)

        // Tắt touch rotate để tránh UX lạ trong ScrollView
        isRotationEnabled = false

        invalidate() // Vẽ lại chart
    }
}


// =============================================
// DIALOG - Hiển thị lịch sử giao dịch theo danh mục
// =============================================
@Composable
private fun TransactionHistoryDialog(
    category: SpendingCategory,
    transactions: List<com.example.mobileapp.domain.model.Transaction>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(AndroidColor.parseColor(category.colorHex)))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lịch sử: ${category.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (transactions.isEmpty()) {
                    Text(
                        text = "Chưa có giao dịch nào",
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    transactions.forEach { transaction ->
                        TransactionItem(transaction)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFF0F0F0)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Color(0xFF26A480))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// =============================================
// COMPOSABLE - 1 dòng giao dịch trong dialog
// =============================================
@Composable
private fun TransactionItem(transaction: com.example.mobileapp.domain.model.Transaction) {
    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(transaction.date))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateStr,
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }
        Text(
            text = formatCurrency(transaction.amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEF5350)
        )
    }
}
