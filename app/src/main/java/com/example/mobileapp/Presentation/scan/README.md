# Chức năng Scan Hóa Đơn

## Tổng quan

Chức năng scan hóa đơn cho phép người dùng chụp ảnh hóa đơn và tự động trích xuất thông tin như:
- Tổng tiền
- Tên cửa hàng
- Ngày tháng
- Danh sách items

## Công nghệ sử dụng

### 1. CameraX
- Thư viện camera hiện đại của Android
- Hỗ trợ preview và capture ảnh
- Tương thích với nhiều thiết bị

### 2. ML Kit Text Recognition
- Google ML Kit để nhận dạng text từ ảnh
- Hoạt động offline (không cần internet)
- Hỗ trợ nhiều ngôn ngữ (Tiếng Việt, Tiếng Anh)

## Cấu trúc

```
presentation/scan/
├── ReceiptScanScreen.kt       # UI màn hình scan
├── ReceiptScanViewModel.kt    # ViewModel xử lý logic
└── README.md                  # Tài liệu này

domain/
├── model/
│   └── ReceiptScanResult.kt   # Model kết quả scan
└── usecase/
    └── ScanReceiptUseCase.kt  # Business logic scan và phân tích
```

## Luồng hoạt động

1. **Người dùng mở màn hình thêm giao dịch**
2. **Nhấn nút "Scan"** → Mở camera
3. **Chụp ảnh hóa đơn** → ML Kit phân tích text
4. **Hiển thị kết quả** → Người dùng xác nhận
5. **Tự động điền thông tin** vào form giao dịch

## Thuật toán phân tích hóa đơn

### Trích xuất tổng tiền
```kotlin
// Tìm các từ khóa: "Total", "Tổng", "Thành tiền"
// Lấy số tiền ở dòng đó hoặc dòng tiếp theo
// Hỗ trợ format: 100,000 | 100.000 | 100000
```

### Trích xuất tên cửa hàng
```kotlin
// Lấy dòng đầu tiên có độ dài hợp lý (3-50 ký tự)
```

### Trích xuất ngày tháng
```kotlin
// Tìm pattern: DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD
```

### Trích xuất items
```kotlin
// Lọc các dòng có số tiền nhưng không phải dòng tổng
```

## Cách sử dụng

### Trong AddTransactionScreen

```kotlin
var showScanScreen by remember { mutableStateOf(false) }

if (showScanScreen) {
    ReceiptScanScreen(
        onNavigateBack = { showScanScreen = false },
        onScanComplete = { result ->
            // Điền thông tin từ scan
            result.totalAmount?.let { vm.setAmount(it.toString()) }
            result.merchantName?.let { vm.setNote(it) }
            showScanScreen = false
        }
    )
}
```

## Quyền cần thiết

Đã được thêm vào `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

## Dependencies

Đã được thêm vào `build.gradle.kts`:

```kotlin
// CameraX
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// ML Kit Text Recognition
implementation("com.google.mlkit:text-recognition:16.0.0")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## Cải tiến trong tương lai

1. **Hỗ trợ nhiều loại hóa đơn hơn**
   - Hóa đơn điện tử (QR code)
   - Hóa đơn siêu thị
   - Hóa đơn nhà hàng

2. **Tự động phân loại danh mục**
   - Dựa vào tên cửa hàng để gợi ý danh mục
   - VD: "Circle K" → "Mua sắm"

3. **Lưu ảnh hóa đơn**
   - Lưu ảnh gốc để tham khảo sau
   - Đính kèm vào giao dịch

4. **Cải thiện độ chính xác**
   - Sử dụng ML Kit Document Scanner
   - Tiền xử lý ảnh (crop, enhance)
   - Custom ML model cho hóa đơn Việt Nam

## Xử lý lỗi

- **Không có quyền camera**: Hiển thị thông báo yêu cầu cấp quyền
- **Scan thất bại**: Cho phép chụp lại
- **Không tìm thấy thông tin**: Hiển thị raw text để người dùng tự nhập

## Testing

### Test thủ công
1. Chụp hóa đơn rõ nét
2. Kiểm tra các format số tiền khác nhau
3. Test với hóa đơn tiếng Việt và tiếng Anh
4. Test trong điều kiện ánh sáng khác nhau

### Test cases
- Hóa đơn siêu thị (nhiều items)
- Hóa đơn nhà hàng (có tip, tax)
- Hóa đơn điện tử (format khác)
- Hóa đơn mờ/nghiêng

## Tham khảo

- [CameraX Documentation](https://developer.android.com/training/camerax)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Jetpack Compose Camera](https://developer.android.com/jetpack/compose/libraries#camera)
