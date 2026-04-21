package com.example.mobileapp.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class chứa kết quả sau khi scan hóa đơn
 */
data class ScanResult(
    val merchantName: String? = null,
    val totalAmount: Double? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanScreen(
    onNavigateBack: () -> Unit,
    onScanComplete: (ScanResult) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quét hóa đơn") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Giao diện Camera quét hóa đơn",
                color = Color.White,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    // Giả lập kết quả quét được
                    onScanComplete(
                        ScanResult(
                            merchantName = "Cửa hàng Tiện Lợi",
                            totalAmount = 50000.0
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DC98E))
            ) {
                Text("Bấm để giả lập Quét thành công")
            }
        }
    }
}
