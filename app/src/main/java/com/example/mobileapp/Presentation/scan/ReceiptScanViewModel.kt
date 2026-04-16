package com.example.mobileapp.presentation.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.ReceiptScanResult
import com.example.mobileapp.domain.usecase.ScanReceiptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình scan hóa đơn
 */
class ReceiptScanViewModel : ViewModel() {

    private val scanReceiptUseCase = ScanReceiptUseCase()

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /**
     * Scan hóa đơn từ bitmap
     */
    fun scanReceipt(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Loading
            
            val result = scanReceiptUseCase.execute(bitmap)
            
            _uiState.value = if (result.isSuccess) {
                ScanUiState.Success(result.getOrNull()!!)
            } else {
                ScanUiState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Reset về trạng thái ban đầu
     */
    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        scanReceiptUseCase.cleanup()
    }
}

/**
 * UI State cho màn hình scan
 */
sealed class ScanUiState {
    object Idle : ScanUiState()
    object Loading : ScanUiState()
    data class Success(val result: ReceiptScanResult) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}
