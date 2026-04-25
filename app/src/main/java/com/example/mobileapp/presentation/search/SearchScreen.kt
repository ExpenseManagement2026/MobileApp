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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ── Màn hình chính ───────────────────────────────────────────────────
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(
            repository = RepositoryProvider.provideTransactionRepository(LocalContext.current),
            filterTransactionsUseCase = RepositoryProvider.filterTransactionsUseCase
        )
    )
) {
    val transactions by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.currentFilterState.collectAsState()
    val searchQuery by viewModel.searchQueryState.collectAsState()

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
            currentFilter = currentFilter,
            onFilterSelected = viewModel::setFilter,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (transactions.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                TransactionList(
                    transactions = transactions,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color(0xFF2ECC71),
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
    FilterItem(FilterType.TRANSFER, "Chuyển khoản"),
    FilterItem(FilterType.CASH,     "Tiền mặt")
)

@Composable
private fun FilterChipRow(
    currentFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filterItems) { item ->
            val isSelected = item.type == currentFilter

            val bgColor = when {
                isSelected && item.type == FilterType.EXPENSE -> Color(0xFFF44336)
                isSelected -> Color(0xFF2ECC71)
                else -> MaterialTheme.colorScheme.surface
            }
            val textColor = when {
                isSelected -> Color.White
                item.type == FilterType.INCOME  -> Color(0xFF2ECC71)
                item.type == FilterType.EXPENSE -> Color(0xFFF44336)
                else -> Color(0xFF757575)
            }
            val borderColor = when (item.type) {
                FilterType.INCOME  -> Color(0xFF2ECC71)
                FilterType.EXPENSE -> Color(0xFFF44336)
                else -> if (isSelected) Color(0xFF2ECC71) else Color(0xFFE0E0E0)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .height(36.dp)
                    .clickable { onFilterSelected(item.type) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = item.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Danh sách giao dịch ──────────────────────────────────────────────
@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions, key = { it.id }) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

// ── Item giao dịch ───────────────────────────────────────────────────
@Composable
private fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type.name == "INCOME"
    val amountColor = if (isIncome) Color(0xFF2ECC71) else Color(0xFFF44336)
    val iconBg = if (isIncome) Color(0xFF2ECC71).copy(alpha = 0.12f)
    else Color(0xFFF44336).copy(alpha = 0.12f)
    val amountPrefix = if (isIncome) "+ " else "- "

    val formattedAmount = remember(transaction.amount) {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        formatter.maximumFractionDigits = 0
        "${amountPrefix}${formatter.format(transaction.amount)} đ"
    }

    val formattedDate = remember(transaction.date) {
        SimpleDateFormat("dd 'thg' M, HH:mm", Locale("vi"))
            .format(Date(transaction.date))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
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
                Text(
                    text = formattedAmount,
                    color = amountColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formattedDate,
                    color = Color(0xFFBDBDBD),
                    fontSize = 11.sp
                )
            }
        }
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
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEEEEE)),
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
        Text(
            text = "Không tìm thấy giao dịch",
            color = Color(0xFF9E9E9E),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thử tìm kiếm với từ khóa khác\nhoặc thay đổi bộ lọc",
            color = Color(0xFFBDBDBD),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}