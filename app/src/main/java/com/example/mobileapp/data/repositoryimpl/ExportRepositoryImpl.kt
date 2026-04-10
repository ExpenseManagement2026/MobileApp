package com.example.mobileapp.data.repositoryimpl

import android.content.Context
import android.os.Environment
import com.example.mobileapp.domain.model.Transaction
import com.example.mobileapp.domain.repository.ExportRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExportRepositoryImpl(
    private val context: Context
) : ExportRepository {

    override suspend fun exportTransactionsToCSV(transactions: List<Transaction>): Result<String> {
        return try {
            // 1. Tạo tên file độc nhất theo thời gian: ChiTieu_20260409_2030.csv
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "BaoCaoChiTieu_$timeStamp.csv"

            // 2. Xác định vị trí lưu: Thư mục Downloads công khai
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            // 3. Tiến hành ghi dữ liệu
            file.printWriter().use { writer ->
                // Ghi dòng tiêu đề (Header) - Có dấu BOM để Excel hiểu tiếng Việt có dấu
                writer.println("\uFEFFTiêu đề,Số tiền,Loại,Danh mục,Ngày,Ghi chú")

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                transactions.forEach { trans ->
                    val dateStr = dateFormat.format(Date(trans.dateMillis))
                    // Thay thế dấu phẩy trong note/title để tránh làm lệch cột CSV
                    val safeTitle = trans.title.replace(",", "-")
                    val safeNote = trans.note.replace(",", "-")

                    writer.println("$safeTitle,${trans.amount},${trans.type},${trans.categoryId},$dateStr,$safeNote")
                }
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}