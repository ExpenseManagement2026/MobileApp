package com.example.mobileapp.presentation.budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel()
) {
    val context = LocalContext.current
    val budgetText by viewModel.budgetText.observeAsState("0 đ")
    val spentText by viewModel.spentText.observeAsState("0 đ")
    val remainingText by viewModel.remainingText.observeAsState("0 đ")
    val percent by viewModel.percent.observeAsState(0)
    val categories by viewModel.categories.observeAsState(emptyList())
    val currentDateText by viewModel.currentDateText.observeAsState("Tháng --/----")
    val message by viewModel.message.observeAsState()

    // MÀU XANH CHUẨN CỦA NHÓM (LẤY TỪ HOME)
    val greenPrimary = Color(0xFF2DC98E)
    val warningColor = Color(0xFFF44336)

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshBudgetData()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(greenPrimary) // Đổi màu Header
                    .padding(24.dp)
            ) {
                Column {
                    Text("Ngân sách của bạn", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(currentDateText, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = greenPrimary), // Đổi màu Card
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tổng ngân sách tháng", color = Color.White.copy(alpha = 0.9f))
                    Text(budgetText, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (percent >= 80) warningColor else Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Text("${percent}%", color = Color.White, modifier = Modifier.align(Alignment.End), fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoBox("Đã chi", spentText, Modifier.weight(1f))
                        InfoBox("Còn lại", remainingText, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Text(
                "Ngân sách theo danh mục",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(categories) { category ->
            CategoryBudgetItem(category)
        }

        item {
            var inputAmount by remember { mutableStateOf("") }
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("Thiết lập ngân sách mới") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

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
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenPrimary), // Đổi màu Nút
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cập nhật ngân sách")
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetItem(category: CategoryBudget) {
    val icon = if (category.name == "Ăn uống") Icons.Filled.Restaurant else Icons.Filled.DirectionsCar
    
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${category.spent} đ / ${category.limit} đ", fontSize = 12.sp, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("${category.percent}%", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(category.status, fontSize = 11.sp, color = Color(android.graphics.Color.parseColor(category.color)))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { category.percent / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(android.graphics.Color.parseColor(category.color)),
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
