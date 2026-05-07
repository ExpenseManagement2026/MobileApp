# 💰 MobileApp - Quản lý Chi tiêu Cá nhân

Ứng dụng Android giúp theo dõi thu chi, quản lý ngân sách và phân tích tài chính cá nhân. Xây dựng bằng Jetpack Compose theo kiến trúc Clean Architecture.

---

## Tính năng

- **Tổng quan tài chính** — Xem số dư, thu nhập, chi tiêu theo từng tháng với biểu đồ cột theo ngày
- **Thêm / Sửa giao dịch** — Nhập thu chi với danh mục, ghi chú, ngày tháng; hỗ trợ scan hóa đơn bằng camera
- **Scan hóa đơn** — Chụp ảnh hóa đơn, tự động trích xuất số tiền, tên cửa hàng và điền vào form (CameraX + ML Kit, hoạt động offline)
- **Lịch sử & Tìm kiếm** — Lọc giao dịch theo hôm nay, tuần, tháng, loại thu/chi hoặc chọn tháng cụ thể
- **Ngân sách** — Đặt ngân sách tháng, theo dõi tiến độ chi tiêu theo danh mục với cảnh báo vượt ngân sách
- **Dashboard** — Biểu đồ tròn chi tiêu theo danh mục, xem lịch sử giao dịch từng danh mục
- **Cài đặt** — Chuyển đổi Dark/Light mode, xóa toàn bộ dữ liệu

---

## Công nghệ

| Thành phần | Thư viện |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Bottom Navigation (custom) |
| ViewModel | AndroidX ViewModel + StateFlow |
| Database | Room (SQLite) |
| Camera | CameraX 1.3.1 |
| OCR | ML Kit Text Recognition 16.0.0 |
| Biểu đồ | MPAndroidChart |
| Image loading | Coil 2.5.0 |
| Async | Kotlin Coroutines |

---

## Kiến trúc

Project theo **Clean Architecture** với 3 layer:

```
Presentation  →  Domain  ←  Data
(Compose UI)     (Models,    (Room DB,
(ViewModel)       UseCases,   Repository Impl)
                  Repository
                  Interface)
```

```
com.example.mobileapp/
├── domain/
│   ├── model/          # Transaction, Statistics, ReceiptScanResult
│   ├── repository/     # TransactionRepository (interface)
│   └── usecase/        # CheckBudgetUseCase, ScanReceiptUseCase, FilterTransactionsUseCase
├── data/
│   ├── local/          # Room Entity, DAO, AppDatabase
│   ├── mapper/         # TransactionMapper (Entity ↔ Domain)
│   ├── repository/     # TransactionRepositoryImpl
│   └── di/             # RepositoryProvider
└── presentation/
    ├── home/           # Màn hình tổng quan
    ├── add/            # Thêm / Sửa giao dịch
    ├── search/         # Lịch sử & tìm kiếm
    ├── budget/         # Quản lý ngân sách
    ├── dashboard/      # Thống kê & biểu đồ
    ├── scan/           # Scan hóa đơn
    ├── settings/       # Cài đặt
    └── theme/          # Theme & ThemePreferences
```

---

## Yêu cầu

- Android Studio Hedgehog trở lên
- JDK 17
- Android SDK 24+ (minSdk 24, targetSdk 35)
- Thiết bị/emulator có camera (để dùng tính năng scan)

---

## Build & Chạy

```bash
# Clone project
git clone <repo-url>

# Build debug
./gradlew assembleDebug

# Cài lên thiết bị
./gradlew installDebug
```

Hoặc mở project trong Android Studio và nhấn **Run** (Shift+F10).

---

## Quyền ứng dụng

| Quyền | Mục đích |
|---|---|
| `CAMERA` | Scan hóa đơn |
| `READ_EXTERNAL_STORAGE` | Đọc ảnh từ thư viện |
| `WRITE_EXTERNAL_STORAGE` | Lưu file (Android ≤ 9) |
| `INTERNET` | Dự phòng cho tính năng tương lai |

---

## Đóng góp

1. Fork repository
2. Tạo branch mới: `git checkout -b feature/ten-tinh-nang`
3. Commit thay đổi: `git commit -m "feat: mô tả ngắn"`
4. Push và tạo Pull Request
