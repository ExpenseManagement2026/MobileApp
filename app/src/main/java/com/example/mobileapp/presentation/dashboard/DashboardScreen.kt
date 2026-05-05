package com.example.mobileapp.presentation.dashboard

import android.app.Application
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.model.TransactionType
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} đ"
}

private fun monthYearText(month: Int, year: Int): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.YEAR, year)
    return java.text.SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN")).format(cal.time)
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            application = context.applicationContext as Application
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf<SpendingCategory?>(null) }

    val greenColor = Color(0xFF26A480)
    val headerGreen = Color(0xFF2ECC9A)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(greenColor)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                // Month picker
                MonthPicker(
                    month = uiState.selectedMonth.month,
                    year = uiState.selectedMonth.year,
                    isCurrentMonth = viewModel.isCurrentMonth(),
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() },
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = headerGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Tổng chi tiêu ${monthYearText(uiState.selectedMonth.month, uiState.selectedMonth.year)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                formatCurrency(uiState.totalExpense),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Tổng thu / tổng chi ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardSummaryCard(Modifier.weight(1f), "Tổng Thu", uiState.totalIncome, uiState.isLoading, Color(0xFF26A480))
            DashboardSummaryCard(Modifier.weight(1f), "Tổng Chi", uiState.totalExpense, uiState.isLoading, Color(0xFFEF5350))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── PieChart ────────────────────────────────────────────────────────
        DashboardCard(title = "Chi tiêu theo danh mục") {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = greenColor)
                }
            } else if (uiState.pieEntries.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Không có chi tiêu trong tháng này",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    factory = { ctx -> PieChart(ctx).apply { configurePieChart(this, uiState) } },
                    update = { chart -> configurePieChart(chart, uiState) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Top danh mục ────────────────────────────────────────────────────
        DashboardCard(title = "Top chi tiêu") {
            if (uiState.isLoading) {
                SkeletonRows(3)
            } else if (uiState.topCategories.isEmpty()) {
                Text(
                    "Không có dữ liệu",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                uiState.topCategories.forEachIndexed { index, category ->
                    CategoryRow(category, uiState.totalExpense) { selectedCategory = category }
                    if (index < uiState.topCategories.lastIndex)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Tất cả danh mục ─────────────────────────────────────────────────
        DashboardCard(title = "Tất cả danh mục") {
            if (uiState.isLoading) {
                SkeletonRows(5)
            } else if (uiState.allCategories.isEmpty()) {
                Text(
                    "Không có dữ liệu",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                uiState.allCategories.forEachIndexed { index, category ->
                    CategoryRow(category, uiState.totalExpense) { selectedCategory = category }
                    if (index < uiState.allCategories.lastIndex)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    selectedCategory?.let { category ->
        TransactionHistoryDialog(
            category = category,
            transactions = viewModel.getTransactionsByCategory(category.name),
            onDismiss = { selectedCategory = null }
        )
    }
}

// ── Month Picker ─────────────────────────────────────────────────────────────

@Composable
private fun MonthPicker(
    month: Int,
    year: Int,
    isCurrentMonth: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Tháng trước",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = monthYearText(month, year),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(
            onClick = onNext,
            enabled = !isCurrentMonth
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Tháng sau",
                tint = if (isCurrentMonth) Color.White.copy(alpha = 0.3f) else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ── Shared composables ───────────────────────────────────────────────────────

@Composable
private fun DashboardCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SkeletonRows(count: Int) {
    repeat(count) {
        Box(
            Modifier.fillMaxWidth().height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DashboardSummaryCard(
    modifier: Modifier,
    label: String,
    amount: Long,
    isLoading: Boolean,
    containerColor: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(formatCurrency(amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun CategoryRow(category: SpendingCategory, totalExpense: Long, onClick: () -> Unit) {
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
        Box(Modifier.size(12.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            category.name,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "%.1f%%".format(percent),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            formatCurrency(category.amount),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun configurePieChart(chart: PieChart, state: DashboardUiState) {
    if (state.pieEntries.isEmpty()) return
    val dataSet = PieDataSet(state.pieEntries, "").apply {
        colors = state.pieColors
        sliceSpace = 3f; selectionShift = 6f; valueTextSize = 0f
    }
    chart.apply {
        data = PieData(dataSet)
        isDrawHoleEnabled = true; holeRadius = 58f; transparentCircleRadius = 62f
        setHoleColor(AndroidColor.WHITE)
        centerText = "${formatCurrency(state.totalExpense)}\nTổng chi tiêu"
        setCenterTextSize(15f)
        setCenterTextColor(AndroidColor.parseColor("#212121"))
        setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        legend.isEnabled = false; description.isEnabled = false
        animateY(1000, Easing.EaseInOutQuad); isRotationEnabled = false
        invalidate()
    }
}

@Composable
private fun TransactionHistoryDialog(
    category: SpendingCategory,
    transactions: List<Transaction>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(16.dp).clip(CircleShape)
                        .background(Color(AndroidColor.parseColor(category.colorHex)))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lịch sử: ${category.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                if (transactions.isEmpty()) {
                    Text(
                        "Chưa có giao dịch nào",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    transactions.forEach { tx ->
                        DialogTransactionItem(tx)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng", color = Color(0xFF26A480)) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DialogTransactionItem(transaction: Transaction) {
    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(transaction.date))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(dateStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Text(
            formatCurrency(transaction.amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFEF5350) else Color(0xFF26A480)
        )
    }
}
