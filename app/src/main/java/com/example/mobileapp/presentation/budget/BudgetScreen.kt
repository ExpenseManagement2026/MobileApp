package com.example.mobileapp.presentation.budget

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.domain.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val GreenPrimary = Color(0xFF2DC98E)
private val YellowWarning = Color(0xFFFFEB3B)
private val OrangeWarning = Color(0xFFFF9800)
private val RedDanger = Color(0xFFF44336)
private val WarningColor = Color(0xFFF44336) // Thêm từ main

@Composable
fun BudgetScreen() {
    val context = LocalContext.current
    
    val viewModel: BudgetViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BudgetViewModel(context.applicationContext as Application) as T
            }
        }
    )
    
    val state by viewModel.state.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
            ) {
                // --- HEADER ---
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(GreenPrimary).padding(24.dp)) {
                        Column {
                            Text("Ngân sách của bạn", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            // Month selector (từ main)
                            MonthSelector(
                                selectedMonth = selectedMonth,
                                onPreviousMonth = { viewModel.previousMonth() },
                                onNextMonth = { viewModel.nextMonth() }
                            )
                        }
                    }
                }

                // --- CARD TỔNG QUAN (Kết hợp logic cả hai) ---
                item {
                    val isOverBudget = state.percent > 100
                    val isWarning = state.percent >= 80 && state.percent <= 100
                    val displayPercent = if (isOverBudget) 100 else state.percent
                    
                    val cardColor = when {
                        isOverBudget -> WarningColor
                        isWarning -> Color(0xFFFFA000)
                        else -> GreenPrimary
                    }

                    Card(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Tổng ngân sách tháng", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Text(state.budgetText, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(16.dp))
                            
                            // LOGIC MÀU SẮC CHUẨN (từ HEAD)
                            val barColor = when {
                                state.percent >= 95 -> RedDanger
                                state.percent >= 85 -> OrangeWarning
                                state.percent >= 75 -> YellowWarning
                                else -> Color.White
                            }

                            LinearProgressIndicator(
                                progress = { (state.percent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (isOverBudget) Color.White else barColor,
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
                                Text("${state.percent}%", color = Color.White, fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

                // --- THIẾT LẬP ---
                item {
                    var inputAmount by remember { mutableStateOf("") }
                    val monthText = remember(selectedMonth) {
                        SimpleDateFormat("MM/yyyy", Locale("vi", "VN")).format(selectedMonth.time)
                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), 
                        shape = RoundedCornerShape(16.dp), 
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Cập nhật ngân sách tháng $monthText", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                // --- DANH MỤC ---
                item {
                    Text("Ngân sách theo danh mục", modifier = Modifier.padding(16.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                items(state.categories) { category ->
                    CategoryBudgetItem(category) {
                        selectedCategoryName = category.name 
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }

            // HIỂN THỊ DIALOG CHI TIẾT
            selectedCategoryName?.let { name ->
                val transactionsState = viewModel.getTransactionsByCategory(name).collectAsState(initial = emptyList())
                TransactionHistoryDialog(
                    categoryName = name,
                    transactions = transactionsState.value,
                    onDismiss = { selectedCategoryName = null }
                )
            }
        }
    }
}

@Composable
fun TransactionHistoryDialog(categoryName: String, transactions: List<Transaction>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lịch sử: $categoryName", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                if (transactions.isEmpty()) {
                    Text("Chưa có giao dịch.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    transactions.forEach { tx ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date(tx.date))
                                Text(text = dateStr, color = Color.Gray, fontSize = 12.sp)
                            }
                            Text(text = "- ${formatMoney(tx.amount)}", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng", color = GreenPrimary, fontWeight = FontWeight.Bold) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CategoryBudgetItem(category: CategoryBudget, onClick: () -> Unit) {
    val barColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { GreenPrimary }
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                    Text(categoryIcon, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${formatMoney(category.spent)} / ${formatMoney(category.limit)}", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${category.percent}%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(category.status, fontSize = 11.sp, color = barColor)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { (category.percent / 100f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = barColor, trackColor = Color.LightGray.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun BudgetInfoBox(label: String, value: String, modifier: Modifier) {
    Box(modifier = modifier.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

private fun formatMoney(value: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(value)} đ"
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
