package com.example.mobileapp.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.Transaction
import com.example.mobileapp.presentation.home.model.toVndString

private val GreenPrimary = Color(0xFF2DC98E)

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    HomeContent(state = state)
}

@Composable
fun HomeContent(state: HomeState) {
    Scaffold(
        bottomBar = { BottomNavBar(selectedIndex = 0) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            BalanceHeader(
                greeting = state.greeting,
                balance = state.totalBalance,
                income = state.totalIncome,
                expense = state.totalExpense,
            )
            SpendingChart(data = state.chartData)
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Giao dịch gần đây",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            state.recentTransactions.forEach { tx ->
                TransactionItem(transaction = tx)
                HorizontalDivider(
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun BalanceHeader(
    greeting: String,
    balance: Long,
    income: Long,
    expense: Long,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            Text(greeting, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            Text("Tổng số dư", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = balance.toVndString(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    icon = "⬆",
                    label = "Thu nhập",
                    amount = income.toVndString(),
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    icon = "⬇",
                    label = "Chi tiêu",
                    amount = expense.toVndString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun SummaryCard(icon: String, label: String, amount: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp, color = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(amount, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun SpendingChart(data: List<Float>) {
    if (data.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Chi tiêu tháng này", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            drawLineChart(data)
        }
    }
}

fun DrawScope.drawLineChart(data: List<Float>) {
    if (data.size < 2) return
    val maxVal = data.max()
    val padLeft = 40.dp.toPx()
    val padBottom = 24.dp.toPx()
    val chartW = size.width - padLeft
    val chartH = size.height - padBottom
    val stepX = chartW / (data.size - 1)

    fun xOf(i: Int) = padLeft + i * stepX
    fun yOf(v: Float) = chartH - (v / maxVal) * chartH

    val points = data.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

    // Fill
    val fillPath = Path().apply {
        moveTo(points.first().x, chartH)
        points.forEach { lineTo(it.x, it.y) }
        lineTo(points.last().x, chartH)
        close()
    }
    drawPath(
        fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(GreenPrimary.copy(alpha = 0.35f), Color.Transparent),
            startY = 0f, endY = chartH,
        )
    )

    // Line
    val linePath = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(linePath, color = GreenPrimary, style = Stroke(width = 3.dp.toPx()))

    // Dots
    points.forEach { pt ->
        drawCircle(color = GreenPrimary, radius = 5.dp.toPx(), center = pt)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pt)
    }

    // X axis
    drawLine(Color.LightGray, Offset(padLeft, chartH), Offset(size.width, chartH), strokeWidth = 1.dp.toPx())
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val amountColor = if (transaction.amount < 0) Color.Red else Color(0xFF2DC98E)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center,
        ) {
            Text(transaction.icon, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(transaction.category, color = Color.Gray, fontSize = 13.sp)
        }
        Text(
            text = transaction.amount.toVndString(),
            color = amountColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun BottomNavBar(selectedIndex: Int) {
    val items = listOf(
        Icons.Default.Home to "Trang chủ",
        Icons.Default.Add to "Thêm",
        Icons.Default.ShowChart to "Báo cáo",
        Icons.Default.AccountBalanceWallet to "Ngân sách",
        Icons.Default.Person to "Cá nhân",
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {},
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    indicatorColor = GreenPrimary.copy(alpha = 0.12f),
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeContentPreview() {
    val previewState = HomeState(
        greeting = "Xin chào,",
        totalBalance = 28_450_000L,
        totalIncome = 15_200_000L,
        totalExpense = 6_750_000L,
        chartData = listOf(100f, 180f, 220f, 310f, 290f, 340f, 380f, 420f),
        recentTransactions = listOf(
            Transaction("tx1", "🍜", "Ăn trưa", "Ăn uống", -85_000),
            Transaction("tx2", "🚕", "Grab về nhà", "Di chuyển", -45_000),
            Transaction("tx3", "🛒", "Siêu thị", "Mua sắm", -120_000),
        ),
    )
    MaterialTheme { HomeContent(state = previewState) }
}
