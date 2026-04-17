package com.example.mobileapp.domain.model

/**
 * Kết quả scan hóa đơn
 */
data class ReceiptScanResult(
    val totalAmount: Double? = null,
    val merchantName: String? = null,
    val date: String? = null,
    val items: List<String> = emptyList(),
    val rawText: String = ""
)
