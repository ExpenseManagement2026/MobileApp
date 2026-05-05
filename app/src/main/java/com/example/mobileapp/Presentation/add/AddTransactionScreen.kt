package com.example.mobileapp.presentation.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.presentation.scan.ReceiptScanScreen
import java.text.SimpleDateFormat
import java.util.*

private val GreenColor = Color(0xFF2DC98E)
private val RedColor   = Color(0xFFFF7676)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,  // null = Add mode, not null = Edit mode
    vm: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.Factory(
            LocalContext.current.applicationContext as android.app.Application
        )
    ),
    onSaved: () -> Unit = {},
) {
    val state by vm.state.collectAsState()
    var showScanScreen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Load transaction for editing or reset for adding
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            vm.loadTransaction(transactionId)
        } else {
            vm.resetState()
        }
    }

    // Khi lưu thành công, gọi callback và đánh dấu đã xử lý
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
            vm.markSavedHandled()
        }
    }

    // Hiển thị màn hình scan nếu được yêu cầu
    if (showScanScreen) {
        ReceiptScanScreen(
            onNavigateBack = { showScanScreen = false },
            onScanComplete = { result ->
                // Điền thông tin từ scan vào form
                result.totalAmount?.let { amount ->
                    vm.setAmount(amount.toLong().toString())
                }
                result.merchantName?.let { merchant ->
                    vm.setNote(merchant)
                }
                showScanScreen = false
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tiêu đề và nút scan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.transactionId != null) "Sửa giao dịch" else "Thêm giao dịch",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            
            // Nút scan hóa đơn (chỉ hiện khi thêm mới)
            if (state.transactionId == null) {
                OutlinedButton(
                    onClick = { showScanScreen = true },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.isExpense) RedColor else GreenColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (state.isExpense) RedColor else GreenColor
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan hóa đơn",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan", fontSize = 14.sp)
                }
            }
        }

        // Toggle Chi tiêu / Thu nhập
        TypeToggle(
            isExpense = state.isExpense,
            onToggle = { vm.setType(it) }
        )

        // Nhập số tiền
        AmountInput(
            amount = state.amount,
            isExpense = state.isExpense,
            onAmountChange = { vm.setAmount(it) }
        )

        // Chọn danh mục
        val categories = if (state.isExpense) expenseCategories else incomeCategories
        Text("Danh mục", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        CategoryGrid(
            categories = categories,
            selected = state.selectedCategory,
            isExpense = state.isExpense,
            onSelect = { vm.setCategory(it) }
        )

        // Ghi chú
        OutlinedTextField(
            value = state.note,
            onValueChange = { vm.setNote(it) },
            label = { Text("Ghi chú (tuỳ chọn)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 2,
        )

        // Chọn ngày
        DateSelector(
            selectedDate = state.selectedDate,
            isExpense = state.isExpense,
            onClick = { showDatePicker = true }
        )

        // Lỗi
        if (state.error != null) {
            Text(state.error!!, color = Color.Red, fontSize = 13.sp)
        }

        // Nút lưu
        Button(
            onClick = { vm.save() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isExpense) RedColor else GreenColor
            ),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (state.transactionId != null) "Cập nhật" else "Lưu giao dịch",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Spacer để đảm bảo nút không bị che bởi bottom navigation
        Spacer(modifier = Modifier.height(80.dp))
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { vm.setDate(it) }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ─── Toggle Chi tiêu / Thu nhập ──────────────────────────────────────────────

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
private fun ToggleButton(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
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

// ─── Nhập số tiền ─────────────────────────────────────────────────────────────

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
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            ),
            singleLine = true,
        )
    }
}

// ─── Grid danh mục ────────────────────────────────────────────────────────────

@Composable
private fun DateSelector(
    selectedDate: Long,
    isExpense: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (isExpense) RedColor else GreenColor
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
    val formattedDate = dateFormat.format(Date(selectedDate))
    
    Column {
        Text("Ngày giao dịch", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(accentColor)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Chọn ngày",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Thay đổi",
                    fontSize = 13.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Grid danh mục ────────────────────────────────────────────────────────────

@Composable
private fun CategoryGrid(
    categories: List<Pair<String, String>>,
    selected: String,
    isExpense: Boolean,
    onSelect: (String) -> Unit,
) {
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
