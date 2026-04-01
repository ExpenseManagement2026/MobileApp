package com.example.mobileapp.ui.budget


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobileapp.data.local.BudgetPreferences
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetScreen() {
    val context = LocalContext.current
    val prefs = remember { BudgetPreferences(context) }

    var inputBudget by remember { mutableStateOf("") }
    var savedBudget by remember { mutableLongStateOf(prefs.getBudget()) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quản lý ngân sách",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Budget hiện tại: ${formatMoney(savedBudget)}"
        )

        OutlinedTextField(
            value = inputBudget,
            onValueChange = { inputBudget = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nhập budget") },
            singleLine = true
        )

        Button(
            onClick = {
                val amount = inputBudget.toLongOrNull()
                if (amount == null || amount <= 0L) {
                    message = "Nhập budget hợp lệ"
                } else {
                    prefs.saveBudget(amount)
                    savedBudget = prefs.getBudget()
                    message = "Lưu budget thành công"
                    inputBudget = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu budget")
        }

        if (message.isNotEmpty()) {
            Text(text = message)
        }
    }
}

private fun formatMoney(value: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(value)} đ"
}