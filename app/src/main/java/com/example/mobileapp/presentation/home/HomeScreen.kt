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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.presentation.home.model.ChartPoint
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.Transaction
import com.example.mobileapp.presentation.home.model.toVndString

private val GreenPrimary = Color(0xFF2DC98E)

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    vm: HomeViewModel = viewModel(
    factory = HomeViewModel.Factory(
        LocalContext.current.applicationContext as android.app.Application
    )
)) {
    val state by vm.state.collectAsState()
    HomeContent(state = state, onSettingsClick = onSettingsClick)
}

@Composable
fun HomeContent(state: HomeState, onSettingsClick: () -> Unit = {}) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenPrimary)
        }
        return
    }
    if (state.error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error, color = Color.Red)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        BalanceHeader(
            greeting = state.greeting,
            balance = state.totalBalance,
            income = state.totalIncome,
            expense = state.totalExpense,
            onSettingsClick = onSettingsClick,
        )
        SpendingChart(points = state.chartPoints)
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

@Composable
fun BalanceHeader(greeting: String, balance: Long, income: Long, expense: Long, onSettingsClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        // Icon bánh răng góc trên phải
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Cài đặt",
                tint = Color.White,
            )
        }
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
                SummaryCard("⬆", "Thu nhập", income.toVndString(), Modifier.weight(1f))
                SummaryCard("⬇", "Chi tiêu", expense.toVndString(), Modifier.weight(1f))
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
fun SpendingChart(points: List<ChartPoint>) {
    if (points.size < 2) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Chi tiêu tháng này", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                Text("Chưa có dữ liệu", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 14.sp)
            }
        }
        return
    }

    val maxVal = points.maxOf { it.amount }.coerceAtLeast(1f)
    val onBg = MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Chi tiêu tháng này", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onBg)
        Spacer(Modifier.height(4.dp))
        // Tổng tích lũy cuối cùng
        val lastAmount = points.last().amount
        Text(
            text = "Tổng: ${(lastAmount * 1000).toLong().toVndString()}",
            fontSize = 13.sp,
            color = GreenPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val padLeft  = 52.dp.toPx()
            val padRight = 16.dp.toPx()
            val padTop   = 24.dp.toPx()
            val padBot   = 28.dp.toPx()
            val chartW   = size.width - padLeft - padRight
            val chartH   = size.height - padTop - padBot

            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 10.dp.toPx()
                color = onBg.copy(alpha = 0.55f).toArgb()
            }
            val valuePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 9.dp.toPx()
                color = GreenPrimary.toArgb()
                isFakeBoldText = true
            }

            // ── Trục Y (4 mức) ──────────────────────────────────────
            val ySteps = 4
            repeat(ySteps + 1) { i ->
                val ratio = i.toFloat() / ySteps
                val y = padTop + chartH * (1f - ratio)
                val label = "${(maxVal * ratio / 1000).toInt()}k"
                drawLine(
                    color = onBg.copy(alpha = 0.07f),
                    start = Offset(padLeft, y),
                    end = Offset(padLeft + chartW, y),
                    strokeWidth = 1.dp.toPx()
                )
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(label, 0f, y + 4.dp.toPx(), textPaint)
                }
            }

            // ── Tính toạ độ điểm ────────────────────────────────────
            fun xOf(i: Int) = padLeft + (i.toFloat() / (points.size - 1)) * chartW
            fun yOf(v: Float) = padTop + chartH * (1f - v / maxVal)

            val coords = points.mapIndexed { i, p -> Offset(xOf(i), yOf(p.amount)) }

            // ── Fill gradient ───────────────────────────────────────
            val fillPath = Path().apply {
                moveTo(coords.first().x, padTop + chartH)
                coords.forEach { lineTo(it.x, it.y) }
                lineTo(coords.last().x, padTop + chartH)
                close()
            }
            drawPath(fillPath, brush = Brush.verticalGradient(
                colors = listOf(GreenPrimary.copy(alpha = 0.3f), Color.Transparent),
                startY = padTop, endY = padTop + chartH,
            ))

            // ── Đường line ──────────────────────────────────────────
            val linePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                coords.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(linePath, color = GreenPrimary, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // ── Điểm + nhãn giá trị + nhãn ngày ────────────────────
            coords.forEachIndexed { i, pt ->
                // Dot
                drawCircle(color = GreenPrimary, radius = 4.5.dp.toPx(), center = pt)
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)

                // Nhãn giá trị phía trên điểm
                val amountK = points[i].amount
                val valueLabel = if (amountK >= 1000f) "${"%.0f".format(amountK / 1000)}M"
                                 else "${"%.0f".format(amountK)}k"
                drawIntoCanvas { canvas ->
                    val tw = valuePaint.measureText(valueLabel)
                    val tx = (pt.x - tw / 2).coerceIn(padLeft, padLeft + chartW - tw)
                    canvas.nativeCanvas.drawText(valueLabel, tx, pt.y - 8.dp.toPx(), valuePaint)
                }

                // Nhãn ngày phía dưới trục X
                val dayLabel = "${points[i].day}"
                drawIntoCanvas { canvas ->
                    val tw = textPaint.measureText(dayLabel)
                    val tx = (pt.x - tw / 2).coerceIn(padLeft, padLeft + chartW - tw)
                    canvas.nativeCanvas.drawText(dayLabel, tx, padTop + chartH + 18.dp.toPx(), textPaint)
                }
            }

            // ── Trục X ──────────────────────────────────────────────
            drawLine(
                color = onBg.copy(alpha = 0.15f),
                start = Offset(padLeft, padTop + chartH),
                end = Offset(padLeft + chartW, padTop + chartH),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val amountColor = if (transaction.amount < 0) Color.Red else GreenPrimary
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFF5F5F5)),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeContentPreview() {
    MaterialTheme {
        HomeContent(
            state = HomeState(
                greeting = "Xin chào,",
                totalBalance = 28_450_000L,
                totalIncome = 15_200_000L,
                totalExpense = 6_750_000L,
                chartPoints = listOf(
                    ChartPoint(1, 0f), ChartPoint(5, 85f), ChartPoint(10, 205f),
                    ChartPoint(15, 310f), ChartPoint(20, 490f), ChartPoint(25, 620f),
                    ChartPoint(30, 750f),
                ),
                recentTransactions = listOf(
                    Transaction("tx1", "🍜", "Ăn trưa", "Ăn uống", -85_000),
                    Transaction("tx2", "🚕", "Grab về nhà", "Di chuyển", -45_000),
                ),
            )
        )
    }
}
