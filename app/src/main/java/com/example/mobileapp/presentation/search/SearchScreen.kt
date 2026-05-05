package com.example.mobileapp.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.usecase.search.FilterType
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val GreenColor   = Color(0xFF2ECC71)
private val RedColor     = Color(0xFFF44336)
private val GrayText     = Color(0xFF757575)
private val BorderGray   = Color(0xFFE0E0E0)

// ── Màn hình chính ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onEditTransaction: (Long) -> Unit = {},  // Callback để navigate đến màn hình edit
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(
            repository = RepositoryProvider.provideTransactionRepository(LocalContext.current),
            filterTransactionsUseCase = RepositoryProvider.filterTransactionsUseCase
        )
    )
) {
    val transactions    by viewModel.uiState.collectAsState()
    val currentFilter   by viewModel.currentFilterState.collectAsState()
    val searchQuery     by viewModel.searchQueryState.collectAsState()
    val customMonth     by viewModel.customMonthState.collectAsState()

    // Lấy toàn bộ giao dịch để build danh sách tháng
    val allTransactions by RepositoryProvider
        .provideTransactionRepository(LocalContext.current)
        .getAllTransactions()
        .collectAsState(initial = emptyList())

    val availableMonths = remember(allTransactions) {
        viewModel.getAvailableMonths(allTransactions)
    }

    // Bottom sheet state
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope       = rememberCoroutineScope()
    var showSheet   by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchHeader(transactionCount = transactions.size)

        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )

        FilterChipRow(
            currentFilter   = currentFilter,
            customMonth     = customMonth,
            onFilterSelected = viewModel::setFilter,
            onMonthPickerClick = { showSheet = true },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (transactions.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                TransactionList(
                    transactions = transactions,
                    onEditClick = onEditTransaction,
                    onDeleteClick = { viewModel.deleteTransaction(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // ── Month picker bottom sheet ────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            MonthPickerSheet(
                availableMonths = availableMonths,
                selectedMonth   = customMonth,
                onMonthSelected = { m ->
                    viewModel.setCustomMonth(m.month, m.year)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onClear = {
                    viewModel.setFilter(FilterType.ALL)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            )
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────
@Composable
private fun SearchHeader(transactionCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Lịch sử giao dịch",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$transactionCount giao dịch",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
    }
}

// ── Ô tìm kiếm ───────────────────────────────────────────────────────
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(56.dp),
        placeholder = { Text("Tìm kiếm giao dịch...", color = Color.Gray) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor   = GreenColor,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}

// ── Bộ lọc chip ──────────────────────────────────────────────────────
private data class FilterItem(val type: FilterType, val label: String)

private val filterItems = listOf(
    FilterItem(FilterType.ALL,     "Tất cả"),
    FilterItem(FilterType.TODAY,   "Hôm nay"),
    FilterItem(FilterType.WEEK,    "Tuần này"),
    FilterItem(FilterType.MONTH,   "Tháng này"),
    FilterItem(FilterType.INCOME,  "Thu nhập"),
    FilterItem(FilterType.EXPENSE, "Chi tiêu"),
)

@Composable
private fun FilterChipRow(
    currentFilter: FilterType,
    customMonth: CustomMonthFilter?,
    onFilterSelected: (FilterType) -> Unit,
    onMonthPickerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Các chip filter thông thường
        items(filterItems) { item ->
            val isSelected = item.type == currentFilter
            val bgColor = when {
                isSelected && item.type == FilterType.EXPENSE -> RedColor
                isSelected -> GreenColor
                else -> MaterialTheme.colorScheme.surface
            }
            val textColor = when {
                isSelected -> Color.White
                item.type == FilterType.INCOME  -> GreenColor
                item.type == FilterType.EXPENSE -> RedColor
                else -> GrayText
            }
            val borderColor = when (item.type) {
                FilterType.INCOME  -> GreenColor
                FilterType.EXPENSE -> RedColor
                else -> if (isSelected) GreenColor else BorderGray
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                modifier = Modifier.height(36.dp).clickable { onFilterSelected(item.type) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = item.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Chip chọn tháng
        item {
            val isMonthSelected = currentFilter == FilterType.CUSTOM_MONTH
            val chipLabel = if (isMonthSelected && customMonth != null) {
                formatMonthShort(customMonth.month, customMonth.year)
            } else "Chọn tháng"

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isMonthSelected) GreenColor else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMonthSelected) GreenColor else BorderGray
                ),
                modifier = Modifier.height(36.dp).clickable { onMonthPickerClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (isMonthSelected) Color.White else GrayText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = chipLabel,
                        color = if (isMonthSelected) Color.White else GrayText,
                        fontSize = 13.sp,
                        fontWeight = if (isMonthSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isMonthSelected) Color.White else GrayText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ── Month picker bottom sheet ─────────────────────────────────────────
@Composable
private fun MonthPickerSheet(
    availableMonths: List<CustomMonthFilter>,
    selectedMonth: CustomMonthFilter?,
    onMonthSelected: (CustomMonthFilter) -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE0E0E0))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Chọn tháng",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (selectedMonth != null) {
                TextButton(onClick = onClear) {
                    Text("Xóa bộ lọc", color = GreenColor, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (availableMonths.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Chưa có giao dịch nào",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
        } else {
            // Grid 3 cột
            val rows = availableMonths.chunked(3)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowItems.forEach { m ->
                        val isSelected = selectedMonth?.month == m.month &&
                                         selectedMonth.year == m.year
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clickable { onMonthSelected(m) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GreenColor else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GreenColor else BorderGray
                            ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Tháng ${m.month + 1}",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White
                                                else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "${m.year}",
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    )
                                }
                            }
                        }
                    }
                    // Fill empty slots nếu row không đủ 3
                    repeat(3 - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// ── Danh sách giao dịch ──────────────────────────────────────────────
@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions, key = { it.id }) { transaction ->
            TransactionItem(
                transaction = transaction,
                onEditClick = { onEditClick(transaction.id) },
                onDeleteClick = { onDeleteClick(transaction) }
            )
        }
    }
}

// ── Item giao dịch ───────────────────────────────────────────────────
@Composable
private fun TransactionItem(
    transaction: Transaction,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Transaction) -> Unit
) {
    val isIncome      = transaction.type.name == "INCOME"
    val amountColor   = if (isIncome) GreenColor else RedColor
    val iconBg        = amountColor.copy(alpha = 0.12f)
    val amountPrefix  = if (isIncome) "+ " else "- "

    var showDeleteDialog by remember { mutableStateOf(false) }

    val formattedAmount = remember(transaction.amount) {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        formatter.maximumFractionDigits = 0
        "${amountPrefix}${formatter.format(transaction.amount)} đ"
    }
    val formattedDate = remember(transaction.date) {
        SimpleDateFormat("dd 'thg' M, HH:mm", Locale("vi")).format(Date(transaction.date))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isIncome) "↑" else "↓",
                        fontSize = 20.sp,
                        color = amountColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(13.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = transaction.note,
                            color = Color(0xFF9E9E9E),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = formattedAmount, color = amountColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = formattedDate, color = Color(0xFFBDBDBD), fontSize = 11.sp)
                }
            }
            
            // Action buttons
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onEditClick(transaction.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = GreenColor)
                ) {
                    Text("Sửa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = RedColor)
                ) {
                    Text("Xóa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa giao dịch \"${transaction.title}\" không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(transaction)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = RedColor)
                ) {
                    Text("Xóa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// ── Trạng thái rỗng ──────────────────────────────────────────────────
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Không tìm thấy giao dịch", color = Color(0xFF9E9E9E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Thử tìm kiếm với từ khóa khác\nhoặc thay đổi bộ lọc",
            color = Color(0xFFBDBDBD),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────
private fun formatMonthShort(month: Int, year: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.MONTH, month)
        set(Calendar.YEAR, year)
    }
    return SimpleDateFormat("'Th' MM/yyyy", Locale("vi", "VN")).format(cal.time)
}
