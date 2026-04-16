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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import java.text.NumberFormat
import java.util.Locale
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.mobileapp.domain.model.Transaction

private fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} đ"
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf<SpendingCategory?>(null) }

    val greenColor  = Color(0xFF2DC98E)
    val headerGreen = Color(0xFF26A480)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(greenColor)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "Tháng 4/2024",
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
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = formatCurrency(uiState.totalExpense),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard("Tổng Thu", uiState.totalIncome, uiState.isLoading, Color(0xFF2DC98E), Modifier.weight(1f))
            SummaryCard("Tổng Chi", uiState.totalExpense, uiState.isLoading, Color(0xFFEF5350), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Chi tiêu theo danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxWidth().height(260.dp), Alignment.Center) { CircularProgressIndicator(color = greenColor) }
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        factory = { context -> PieChart(context).apply { configurePieChart(this, uiState) } },
                        update = { chart -> configurePieChart(chart, uiState) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Top chi tiêu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                uiState.topCategories.forEach { category ->
                    CategoryRow(category, uiState.totalExpense) { selectedCategory = category }
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

@Composable
private fun SummaryCard(label: String, amount: Long, isLoading: Boolean, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(10.dp))
        Text(category.name, Modifier.weight(1f), fontSize = 14.sp)
        Text("%.1f%%".format(percent), fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.width(12.dp))
        Text(formatCurrency(category.amount), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun configurePieChart(chart: PieChart, state: DashboardUiState) {
    if (state.pieEntries.isEmpty()) return
    val dataSet = PieDataSet(state.pieEntries, "").apply {
        colors = state.pieColors
        sliceSpace = 3f
        valueTextSize = 0f
    }
    chart.apply {
        data = PieData(dataSet)
        isDrawHoleEnabled = true
        holeRadius = 58f
        centerText = "${formatCurrency(state.totalExpense)}\nTổng chi tiêu"
        legend.isEnabled = false
        description.isEnabled = false
        animateY(1000, Easing.EaseInOutQuad)
        invalidate()
    }
}

@Composable
private fun TransactionHistoryDialog(category: SpendingCategory, transactions: List<Transaction>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lịch sử: ${category.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                transactions.forEach { tx ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(tx.title, Modifier.weight(1f), fontSize = 14.sp)
                        Text(formatCurrency(tx.amount), color = Color.Red, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}
