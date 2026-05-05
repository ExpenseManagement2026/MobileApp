package com.example.mobileapp.presentation.budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

private val GreenPrimary = Color(0xFF2DC98E)
private val WarningColor = Color(0xFFF44336)

@Composable
fun BudgetScreen() {
    val context = LocalContext.current
    val viewModel: BudgetViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                BudgetViewModel(context.applicationContext as android.app.Application) as T
        }
    )
    val state by viewModel.state.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenPrimary)
                    .padding(24.dp)
            ) {
                Column {
                    Text("Ngân sách của bạn", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    // Month selector
                    MonthSelector(
                        selectedMonth = selectedMonth,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() }
                    )
                }
            }
        }

        // Card tổng quan
        item {
            val isOverBudget = state.percent > 100
            val isWarning = state.percent >= 80 && state.percent <= 100
            val displayPercent = if (isOverBudget) 100 else state.percent
            
            val cardColor = when {
                isOverBudget -> WarningColor  // Đỏ khi vượt
                isWarning -> Color(0xFFFFA000)  // Vàng khi 80-100%
                else -> GreenPrimary  // Xanh khi < 80%
            }
            
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tổng ngân sách tháng", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Text(
                        state.budgetText,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { (displayPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isOverBudget) {
                            Text(
                                "⚠️ Vượt ${state.remainingText}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Còn lại ${state.remainingText}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isWarning) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text(
                            if (isOverBudget) "100%" else "${state.percent}%",
                            color = Color.White,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BudgetInfoBox("Đã chi", state.spentText, Modifier.weight(1f))
                        BudgetInfoBox(
                            if (isOverBudget) "Vượt" else "Còn lại",
                            state.remainingText,
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Thiết lập ngân sách
        item {
            var inputAmount by remember { mutableStateOf("") }
            val monthText = remember(selectedMonth) {
                SimpleDateFormat("MM/yyyy", Locale("vi", "VN")).format(selectedMonth.time)
            }
            
            Card(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thiết lập ngân sách", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Tháng $monthText",
                            fontSize = 13.sp,
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Nhập số tiền") },
                        suffix = { Text("đ", color = GreenPrimary, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary,
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val amount = inputAmount.toLongOrNull() ?: 0L
                            if (amount > 0) {
                                viewModel.saveNewBudget(amount)
                                inputAmount = ""
                            } else {
                                Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Cập nhật ngân sách tháng $monthText", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Danh mục
        item {
            Text(
                "Ngân sách theo danh mục",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        if (state.categories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có giao dịch trong tháng này", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            items(state.categories) { category ->
                CategoryBudgetItem(category)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun BudgetInfoBox(label: String, value: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun CategoryBudgetItem(category: CategoryBudget) {
    val barColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) { Color(0xFF2DC98E) }

    val categoryIcon = when (category.name) {
        "Ăn uống"   -> "🍜"
        "Di chuyển" -> "🚕"
        "Mua sắm"   -> "🛒"
        "Hóa đơn"   -> "⚡"
        "Giải trí"  -> "🎮"
        "Sức khỏe"  -> "💊"
        "Giáo dục"  -> "📚"
        else        -> "📦"
    }

    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(categoryIcon, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "${category.spent.formatVnd()} / ${category.limit.formatVnd()}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${category.percent}%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(category.status, fontSize = 11.sp, color = barColor)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (category.percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = Color.LightGray.copy(alpha = 0.3f),
            )
        }
    }
}

private fun Long.formatVnd(): String {
    val abs = Math.abs(this)
    val formatted = abs.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$formatted đ"
}

// ── Month Selector ────────────────────────────────────────────────────
@Composable
private fun MonthSelector(
    selectedMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val sdf = SimpleDateFormat("'Tháng' MM/yyyy", Locale("vi", "VN"))
    val monthText = sdf.format(selectedMonth.time)
    
    // Check if next month is available (not future)
    val now = Calendar.getInstance()
    val canGoNext = selectedMonth.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
            (selectedMonth.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
             selectedMonth.get(Calendar.MONTH) < now.get(Calendar.MONTH))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Tháng trước",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = monthText,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        IconButton(
            onClick = onNextMonth,
            enabled = canGoNext,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Tháng sau",
                tint = if (canGoNext) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
