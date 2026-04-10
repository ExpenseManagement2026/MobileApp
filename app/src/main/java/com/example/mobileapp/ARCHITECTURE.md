# Clean Architecture - Cấu trúc dự án

## Tổng quan

Dự án tuân theo **Clean Architecture** với 3 layer chính:

```
┌─────────────────────────────────────────┐
│   Presentation Layer (UI)               │
│   - Compose UI                          │
│   - ViewModel                           │
│   - State Management                    │
└──────────────┬──────────────────────────┘
               │ depends on
┌──────────────▼──────────────────────────┐
│   Domain Layer (Business Logic)         │
│   - Models (Transaction, Statistics)    │
│   - Repository Interface                │
│   - Use Cases (optional)                │
└──────────────┬──────────────────────────┘
               │ implements
┌──────────────▼──────────────────────────┐
│   Data Layer (Data Source)              │
│   - Room Database                       │
│   - Repository Implementation           │
│   - Entity & Mapper                     │
└─────────────────────────────────────────┘
```

## Cấu trúc thư mục

```
com.example.mobileapp/
├── domain/                          # Domain Layer (Business Logic thuần túy)
│   ├── model/
│   │   ├── Transaction.kt          # Model sạch, không phụ thuộc framework
│   │   └── Statistics.kt
│   └── repository/
│       └── TransactionRepository.kt # Interface định nghĩa contract
│
├── data/                            # Data Layer (Implementation)
│   ├── local/
│   │   ├── entity/
│   │   │   └── TransactionEntity.kt # Room Entity (có annotation)
│   │   ├── dao/
│   │   │   └── TransactionDao.kt    # SQL queries
│   │   └── database/
│   │       └── AppDatabase.kt       # Room Database singleton
│   ├── mapper/
│   │   └── TransactionMapper.kt     # Chuyển đổi Entity <-> Domain
│   ├── repository/
│   │   └── TransactionRepositoryImpl.kt # Implement interface từ Domain
│   └── di/
│       └── RepositoryProvider.kt    # Dependency Injection
│
└── presentation/                    # Presentation Layer (UI)
    ├── dashboard/
    │   ├── DashboardScreen.kt       # Compose UI
    │   └── DashboardViewModel.kt    # State management
    └── ...
```

## Luồng dữ liệu (Data Flow)

### 1. Đọc dữ liệu (Read)
```
UI (Compose)
  ↓ collectAsState()
ViewModel
  ↓ observe Flow
Repository Interface (Domain)
  ↓ implement
Repository Implementation (Data)
  ↓ query
DAO (Room)
  ↓ SQL
SQLite Database
```

### 2. Ghi dữ liệu (Write)
```
UI (Compose)
  ↓ onClick
ViewModel
  ↓ viewModelScope.launch
Repository Interface (Domain)
  ↓ implement
Repository Implementation (Data)
  ↓ insert/update/delete
DAO (Room)
  ↓ SQL
SQLite Database
  ↓ trigger Flow
UI tự động cập nhật
```

## Nguyên tắc Clean Architecture

### 1. Dependency Rule
- **Domain Layer** không phụ thuộc vào ai (trung tâm)
- **Data Layer** phụ thuộc vào Domain (implement interface)
- **Presentation Layer** phụ thuộc vào Domain (dùng interface)

### 2. Separation of Concerns
- **Domain**: Chứa business logic, models thuần túy
- **Data**: Xử lý nguồn dữ liệu (DB, API, Cache)
- **Presentation**: Hiển thị UI, xử lý user interaction

### 3. Testability
- Domain Layer dễ test (không phụ thuộc framework)
- Data Layer test bằng mock DAO
- Presentation Layer test bằng mock Repository

## Ví dụ sử dụng

### Trong ViewModel:

```kotlin
class DashboardViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // Observe dữ liệu từ Repository
    val transactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Thêm giao dịch mới
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
}
```

### Khởi tạo Repository:

```kotlin
// Trong Activity/Composable
val context = LocalContext.current
val repository = remember {
    RepositoryProvider.provideTransactionRepository(context)
}
val viewModel = viewModel<DashboardViewModel>(
    factory = DashboardViewModelFactory(repository)
)
```

## Lợi ích

1. **Dễ maintain**: Mỗi layer có trách nhiệm riêng
2. **Dễ test**: Mock interface thay vì implementation
3. **Dễ scale**: Thêm feature mới không ảnh hưởng code cũ
4. **Dễ thay đổi**: Đổi Room -> Firebase chỉ cần sửa Data Layer
5. **Reusable**: Domain Layer có thể dùng cho backend, KMM, v.v.

## Migration từ code cũ

Nếu đang có code trực tiếp gọi DAO trong ViewModel:

**Trước (Bad):**
```kotlin
class ViewModel(private val dao: TransactionDao) {
    val data = dao.getAllTransactions() // ViewModel phụ thuộc Room
}
```

**Sau (Good):**
```kotlin
class ViewModel(private val repository: TransactionRepository) {
    val data = repository.getAllTransactions() // ViewModel chỉ biết interface
}
```

## Tài liệu tham khảo

- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Room Database](https://developer.android.com/training/data-storage/room)
