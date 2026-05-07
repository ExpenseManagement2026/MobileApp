package com.example.mobileapp.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.presentation.home.model.DayBar
import com.example.mobileapp.presentation.home.model.HomeState
import com.example.mobileapp.presentation.home.model.SelectedMonth
import com.example.mobileapp.presentation.home.model.Transaction
import com.example.mobileapp.presentation.home.model.toVndString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val GreenPrimary  = Color(0xFF2DC98E)
private val ExpenseColor  = Color(0xFFFF6B6B)
private val TodayColor    = Color(0xFFFFB347)

private fun monthYearText(month: Int, year: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.MONTH, month)
        set(Calendar.YEAR, year)
    }
    return SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN")).format(cal.time)
}

private fun Long.toShortString(): String = when {
    this >= 1_000_000_000L -> "${"%.1f".format(this / 1_000_000_000f)}B"
    this >= 1_000_000L     -> "${"%.1f".format(this / 1_000_000f)}M"
    this >= 1_000L         -> "${"%.0f".format(this / 1_000f)}k"
    else                   -> "$this"
}

// ── Screen entry point ────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    vm: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            LocalContext.current.applicationContext as android.app.Application
        )
    ),
) {
    val state by vm.state.collectAsState()
    HomeContent(
        state = state,
        onSettingsClick = onSettingsClick,
        onPreviousMonth = { vm.previousMonth() },
        onNextMonth = { vm.nextMonth() },
        isCurrentMonth = vm.isCurrentMonth(),
    )
}

// ── Main content ──────────────────────────────────────────────────────────────

@Composable
fun HomeContent(
    state: HomeState,
    onSettingsClick: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    isCurrentMonth: Boolean = true,
) {
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
            selectedMonth = state.selectedMonth,
            balance = state.totalBalance,
            income = state.totalIncome,
            expense = state.totalExpense,
            isCurrentMonth = isCurrentMonth,
            onSettingsClick = onSettingsClick,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
        )

        DailyBarChart(
            bars = state.dailyBars,
            month = state.selectedMonth.month,
            year = state.selectedMonth.year,
        )

        HorizontalDivider(color = Color(0xFFF0F0F0))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Giao dịch ${monthYearText(state.selectedMonth.month, state.selectedMonth.year)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        if (state.recentTransactions.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Không có giao dịch nào",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
        } else {
            state.recentTransactions.forEach { tx ->
                TransactionItem(transaction = tx)
                HorizontalDivider(
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ── Balance Header ────────────────────────────────────────────────────────────

@Composable
fun BalanceHeader(
    greeting: String,
    selectedMonth: SelectedMonth,
    balance: Long,
    income: Long,
    expense: Long,
    isCurrentMonth: Boolean,
    onSettingsClick: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // ── Dòng 1: Greeting + Settings ─────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(greeting, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Dòng 2: Month picker ─────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Tháng trước",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = monthYearText(selectedMonth.month, selectedMonth.year),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onNextMonth,
                enabled = !isCurrentMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Tháng sau",
                    tint = if (isCurrentMonth) Color.White.copy(alpha = 0.3f) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Dòng 3: Số dư + Thu/Chi ──────────────────────────────
        Spacer(Modifier.height(4.dp))
        Text("Tổng số dư (tất cả)", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = balance.toVndString(),
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("⬆", "Thu nhập", income.toVndString(), Modifier.weight(1f))
            SummaryCard("⬇", "Chi tiêu", expense.toVndString(), Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(icon: String, label: String, amount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon box xanh nhạt
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, fontSize = 18.sp)
            }
            Column {
                Text(
                    label,
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    amount,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Daily Bar Chart ───────────────────────────────────────────────────────────

@Composable
fun DailyBarChart(
    bars: List<DayBar>,
    month: Int = -1,
    year: Int = -1,
) {
    val monthLabel = if (month >= 0 && year > 0) monthYearText(month, year) else "tháng này"
    val onBg        = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface

    val barWidth   = 22.dp
    val barSpacing = 10.dp
    val itemWidth  = barWidth + barSpacing
    // Chart zone: chỉ phần cột, không tính label
    val barZoneH   = 130.dp
    val dayLabelH  = 20.dp
    val totalH     = barZoneH + dayLabelH

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {

            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Chi tiêu $monthLabel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = onBg,
                    )
                    // Tổng chi tháng
                    val total = bars.sumOf { it.expense }
                    if (total > 0) {
                        Text(
                            "Tổng: ${total.toShortString()}",
                            fontSize = 12.sp,
                            color = ExpenseColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ExpenseColor)
                    )
                    Text(
                        "Chi / ngày",
                        fontSize = 11.sp,
                        color = onBg.copy(alpha = 0.45f),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Chart area ──────────────────────────────────────────
            if (bars.isEmpty() || bars.all { it.expense == 0L }) {
                Box(
                    Modifier.fillMaxWidth().height(totalH),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chưa có chi tiêu nào",
                        color = onBg.copy(alpha = 0.3f),
                        fontSize = 14.sp
                    )
                }
            } else {
                val maxExpense = bars.maxOf { it.expense }.coerceAtLeast(1L).toFloat()
                val density = LocalDensity.current
                val scrollState = rememberScrollState()
                val todayIndex = bars.indexOfFirst { it.isToday }

                LaunchedEffect(bars) {
                    if (todayIndex > 3) {
                        val scrollTo = with(density) {
                            ((todayIndex - 3).coerceAtLeast(0) * itemWidth.toPx()).toInt()
                        }
                        scrollState.animateScrollTo(scrollTo)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    // Đường baseline
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = dayLabelH),
                        thickness = 1.dp,
                        color = onBg.copy(alpha = 0.08f),
                    )

                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 8.dp)
                            .width(itemWidth * bars.size + barSpacing)
                            .height(totalH),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        bars.forEach { bar ->
                            DayBarItem(
                                bar = bar,
                                maxExpense = maxExpense,
                                barWidth = barWidth,
                                barZoneH = barZoneH,
                                dayLabelH = dayLabelH,
                                onBg = onBg,
                            )
                            Spacer(Modifier.width(barSpacing))
                        }
                    }
                }

                // Scroll hint — chỉ hiện khi có nhiều ngày
                if (bars.size > 10) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("←", fontSize = 9.sp, color = onBg.copy(alpha = 0.2f))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Vuốt để xem thêm",
                            fontSize = 10.sp,
                            color = onBg.copy(alpha = 0.25f),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("→", fontSize = 9.sp, color = onBg.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBarItem(
    bar: DayBar,
    maxExpense: Float,
    barWidth: androidx.compose.ui.unit.Dp,
    barZoneH: androidx.compose.ui.unit.Dp,
    dayLabelH: androidx.compose.ui.unit.Dp,
    onBg: Color,
) {
    val isActive = bar.expense > 0
    val barColor = when {
        bar.isToday -> TodayColor
        isActive    -> ExpenseColor
        else        -> Color.Transparent  // ngày không chi: ẩn cột
    }
    val labelColor = if (bar.isToday) TodayColor else ExpenseColor

    val minBarH = 6.dp
    val barH = if (isActive)
        (barZoneH * (bar.expense / maxExpense)).coerceAtLeast(minBarH)
    else 0.dp

    // Cột đủ cao (>= 24dp) → label trắng bên trong
    val labelInside = isActive && barH >= 24.dp

    Column(
        modifier = Modifier
            .width(barWidth)
            .height(barZoneH + dayLabelH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Vùng bar (barZoneH) ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barZoneH),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barH)
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(barColor),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    // Label bên trong cột cao
                    if (labelInside) {
                        Text(
                            text = bar.expense.toShortString(),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }

                // Label phía trên cột thấp
                if (!labelInside) {
                    Text(
                        text = bar.expense.toShortString(),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = barH + 3.dp),
                    )
                }
            }
        }

        // ── Label ngày (dayLabelH) ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dayLabelH),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${bar.day}",
                fontSize = 10.sp,
                color = when {
                    bar.isToday -> TodayColor
                    isActive    -> onBg.copy(alpha = 0.75f)
                    else        -> onBg.copy(alpha = 0.3f)
                },
                fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Transaction Item ──────────────────────────────────────────────────────────

@Composable
fun TransactionItem(transaction: Transaction) {
    val amountColor = if (transaction.amount < 0) Color.Red else GreenPrimary
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

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeContentPreview() {
    MaterialTheme {
        HomeContent(
            state = HomeState(
                greeting = "Xin chào,",
                selectedMonth = SelectedMonth(4, 2026),
                totalBalance = 28_450_000L,
                totalIncome = 15_200_000L,
                totalExpense = 6_750_000L,
                dailyBars = (1..31).map { day ->
                    DayBar(
                        day = day,
                        expense = when {
                            day % 7 == 0 -> 500_000L
                            day % 3 == 0 -> 150_000L
                            day % 2 == 0 -> 80_000L
                            else         -> 0L
                        },
                        isToday = day == 4,
                    )
                },
                recentTransactions = listOf(
                    Transaction("tx1", "🍜", "Ăn trưa", "Ăn uống", -85_000),
                    Transaction("tx2", "🚕", "Grab về nhà", "Di chuyển", -45_000),
                ),
            ),
            isCurrentMonth = false,
        )
    }
}
