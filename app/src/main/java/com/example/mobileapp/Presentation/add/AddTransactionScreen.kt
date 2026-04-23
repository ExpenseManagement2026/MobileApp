package com.example.mobileapp.presentation.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val GreenColor = Color(0xFF2DC98E)
private val RedColor   = Color(0xFFFF7676)

@Composable
fun AddTransactionScreen(
    vm: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.Factory(
            LocalContext.current.applicationContext as android.app.Application
        )
    ),
    onSaved: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.resetState()
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
            vm.resetSaveState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thêm giao dịch",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            
            IconButton(
                onClick = onScanClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan AI",
                    tint = GreenColor
                )
            }
        }

        TypeToggle(
            isExpense = state.isExpense,
            onToggle = { vm.setType(it) }
        )

        AmountInput(
            amount = state.amount,
            isExpense = state.isExpense,
            onAmountChange = { vm.setAmount(it) }
        )

        val categories = if (state.isExpense) expenseCategories else incomeCategories
        Text("Danh mục", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        CategoryGrid(
            categories = categories,
            selected = state.selectedCategory,
            isExpense = state.isExpense,
            onSelect = { vm.setCategory(it) }
        )

        OutlinedTextField(
            value = state.note,
            onValueChange = { vm.setNote(it) },
            label = { Text("Ghi chú (tuỳ chọn)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 2,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        // CHỈ HIỂN THỊ PHƯƠNG THỨC THANH TOÁN KHI LÀ "CHI TIÊU"
        if (state.isExpense) {
            PaymentMethodToggle(
                selected = state.paymentMethod,
                onSelect = { vm.setPaymentMethod(it) }
            )
        }

        if (state.error != null) {
            Text(state.error!!, color = Color.Red, fontSize = 13.sp)
        }

        Button(
            onClick = { vm.save() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isExpense) RedColor else GreenColor
            )
        ) {
            Text("Lưu giao dịch", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PaymentMethodToggle(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    Column {
        Text(text = "Phương thức thanh toán", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .padding(4.dp),
        ) {
            PaymentMethod.entries.forEach { method ->
                val isSelected = selected == method
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GreenColor else Color.Transparent)
                        .clickable { onSelect(method) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(method.icon, fontSize = 16.sp)
                        Text(
                            text = method.label,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeToggle(isExpense: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(4.dp),
    ) {
        ToggleButton(
            label = "Chi tiêu",
            selected = isExpense,
            selectedColor = RedColor,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(true) }
        )
        ToggleButton(
            label = "Thu nhập",
            selected = !isExpense,
            selectedColor = GreenColor,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(false) }
        )
    }
}

@Composable
private fun ToggleButton(label: String, selected: Boolean, selectedColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Gray,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun AmountInput(amount: String, isExpense: Boolean, onAmountChange: (String) -> Unit) {
    val accentColor = if (isExpense) RedColor else GreenColor
    Column {
        Text("Số tiền", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { onAmountChange(it.filter { c -> c.isDigit() }) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("0", color = Color.LightGray) },
            suffix = { Text("đ", color = accentColor, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                focusedLabelColor = accentColor,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            ),
            singleLine = true,
        )
    }
}

@Composable
private fun CategoryGrid(categories: List<Pair<String, String>>, selected: String, isExpense: Boolean, onSelect: (String) -> Unit) {
    val accentColor = if (isExpense) RedColor else GreenColor
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 240.dp),
        userScrollEnabled = false,
    ) {
        items(categories) { (name, icon) ->
            val isSelected = selected == name
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Color(0xFFF8F8F8))
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) accentColor else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(name) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(icon, fontSize = 22.sp)
                Text(
                    text = name,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) accentColor else Color(0xFF424242),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
}

private val expenseCategories = listOf("Ăn uống" to "🍜", "Di chuyển" to "🚕", "Mua sắm" to "🛒", "Hóa đơn" to "⚡", "Giải trí" to "🎮", "Sức khỏe" to "💊", "Giáo dục" to "📚", "Khác" to "📦")
private val incomeCategories = listOf("Lương" to "💰", "Thưởng" to "🎁", "Đầu tư" to "📈", "Freelance" to "💻", "Khác" to "🪙")
