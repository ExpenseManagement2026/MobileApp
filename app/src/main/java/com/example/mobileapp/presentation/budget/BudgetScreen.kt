package com.example.mobileapp.presentation.budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
                    Text(state.currentDateText, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        // Card tổng quan
        item {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary),
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
                        progress = { (state.percent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (state.percent >= 80) WarningColor else Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                    Text(
                        "${state.percent}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BudgetInfoBox("Đã chi", state.spentText, Modifier.weight(1f))
                        BudgetInfoBox("Còn lại", state.remainingText, Modifier.weight(1f))
                    }
                }
            }
        }

        // Thiết lập ngân sách
        item {
            var inputAmount by remember { mutableStateOf("") }
            Card(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Thiết lập ngân sách", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Text("Cập nhật ngân sách", fontWeight = FontWeight.SemiBold)
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
