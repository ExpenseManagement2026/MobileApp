package com.example.mobileapp.domain.usecase

import android.graphics.Bitmap
import com.example.mobileapp.domain.model.ReceiptScanResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern

/**
 * Use case để scan và phân tích hóa đơn
 */
class ScanReceiptUseCase {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Scan hóa đơn từ bitmap
     */
    suspend fun execute(bitmap: Bitmap): Result<ReceiptScanResult> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(image).await()
            
            val rawText = visionText.text
            val lines = visionText.textBlocks.flatMap { block -> block.lines }.map { line -> line.text }
            
            // Phân tích text để trích xuất thông tin
            val totalAmount = extractTotalAmount(lines)
            val merchantName = extractMerchantName(lines)
            val date = extractDate(lines)
            val items = extractItems(lines)
            
            Result.success(
                ReceiptScanResult(
                    totalAmount = totalAmount,
                    merchantName = merchantName,
                    date = date,
                    items = items,
                    rawText = rawText
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Trích xuất tổng tiền từ hóa đơn
     * Tìm các pattern như: "Total", "Tổng", "Thành tiền", theo sau là số tiền
     */
    private fun extractTotalAmount(lines: List<String>): Double? {
        val totalKeywords = listOf(
            "total", "tổng", "thành tiền", "tổng cộng", "sum", "amount",
            "grand total", "subtotal", "thanh toan", "thanh toán"
        )
        
        for (i in lines.indices) {
            val line = lines[i].lowercase()
            
            // Kiểm tra nếu dòng chứa từ khóa total
            if (totalKeywords.any { line.contains(it) }) {
                // Tìm số tiền trong dòng hiện tại hoặc dòng tiếp theo
                val amount = extractAmount(lines[i])
                if (amount != null) return amount
                
                if (i + 1 < lines.size) {
                    val nextAmount = extractAmount(lines[i + 1])
                    if (nextAmount != null) return nextAmount
                }
            }
        }
        
        // Nếu không tìm thấy, tìm số tiền lớn nhất
        return lines.mapNotNull { extractAmount(it) }.maxOrNull()
    }

    /**
     * Trích xuất số tiền từ chuỗi
     * Hỗ trợ format: 100,000 | 100.000 | 100000 | $100 | 100đ | 100VND
     */
    private fun extractAmount(text: String): Double? {
        // Pattern để tìm số tiền (hỗ trợ dấu phẩy, chấm, ký hiệu tiền tệ)
        val patterns = listOf(
            "([0-9]{1,3}[,.]?[0-9]{3}[,.]?[0-9]*)",  // 100,000 hoặc 100.000
            "([0-9]+[,.]?[0-9]*)",                     // 100000 hoặc 100.50
        )
        
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern).matcher(text)
            if (matcher.find()) {
                val amountStr = matcher.group(1)
                    ?.replace(",", "")
                    ?.replace(".", "")
                    ?.replace(" ", "")
                
                return amountStr?.toDoubleOrNull()
            }
        }
        
        return null
    }

    /**
     * Trích xuất tên cửa hàng (thường ở dòng đầu tiên)
     */
    private fun extractMerchantName(lines: List<String>): String? {
        // Lấy dòng đầu tiên không rỗng và có độ dài hợp lý
        return lines.firstOrNull { 
            it.isNotBlank() && it.length > 3 && it.length < 50 
        }
    }

    /**
     * Trích xuất ngày tháng
     * Hỗ trợ format: DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD
     */
    private fun extractDate(lines: List<String>): String? {
        val datePatterns = listOf(
            "\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}",  // DD/MM/YYYY hoặc DD-MM-YYYY
            "\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}",    // YYYY-MM-DD
        )
        
        for (line in lines) {
            for (pattern in datePatterns) {
                val matcher = Pattern.compile(pattern).matcher(line)
                if (matcher.find()) {
                    return matcher.group(0)
                }
            }
        }
        
        return null
    }

    /**
     * Trích xuất danh sách items (các dòng có số tiền)
     */
    private fun extractItems(lines: List<String>): List<String> {
        return lines.filter { line ->
            // Lọc các dòng có chứa số tiền và không phải là dòng tổng
            val hasAmount = extractAmount(line) != null
            val isNotTotal = !line.lowercase().let { text ->
                listOf("total", "tổng", "thành tiền", "subtotal").any { text.contains(it) }
            }
            hasAmount && isNotTotal && line.length > 3
        }.take(10) // Giới hạn 10 items
    }

    fun cleanup() {
        recognizer.close()
    }
}
