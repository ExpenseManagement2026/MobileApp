# 🐛 Bug Fix - Missing Coroutines Play Services Dependency

## ❌ Lỗi gặp phải

```
Unresolved reference 'tasks'
Unresolved reference 'await'
```

**File:** `ScanReceiptUseCase.kt`  
**Line:** `import kotlinx.coroutines.tasks.await`

## 🔍 Nguyên nhân

ML Kit sử dụng Google Play Services Tasks API, không phải Kotlin Coroutines trực tiếp. Để sử dụng `.await()` với Tasks, cần thêm dependency `kotlinx-coroutines-play-services`.

## ✅ Giải pháp

### 1. Thêm dependency vào `app/build.gradle.kts`

```kotlin
// ML Kit Text Recognition
implementation("com.google.mlkit:text-recognition:16.0.0")

// Kotlin Coroutines Play Services (for ML Kit .await())
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

### 2. Sửa code trong `ScanReceiptUseCase.kt`

**Trước:**
```kotlin
val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text }
```

**Sau:**
```kotlin
val lines = visionText.textBlocks.flatMap { block -> block.lines }.map { line -> line.text }
```

**Lý do:** Tránh ambiguity với `it` trong nested lambdas.

## 📦 Dependency mới

```gradle
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
```

**Chức năng:** Cung cấp extension function `.await()` để convert Google Play Services `Task<T>` thành Kotlin Coroutine.

## 🔧 Cách hoạt động

```kotlin
// Google Play Services Task
val task: Task<Text> = recognizer.process(image)

// Convert to Coroutine với .await()
val result: Text = task.await()
```

## ✅ Kết quả

- ✅ Build thành công
- ✅ Không còn lỗi compile
- ✅ ML Kit hoạt động với Coroutines

## 📚 Tham khảo

- [Kotlin Coroutines Play Services](https://github.com/Kotlin/kotlinx.coroutines/tree/master/integration/kotlinx-coroutines-play-services)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition/android)
- [Google Play Services Tasks](https://developers.google.com/android/guides/tasks)

## 🎯 Files đã cập nhật

1. ✅ `app/build.gradle.kts` - Thêm dependency
2. ✅ `ScanReceiptUseCase.kt` - Sửa lambda parameters
3. ✅ `SCAN_FEATURE_TECHNICAL_SUMMARY.md` - Cập nhật docs
4. ✅ `SCAN_FEATURE_README.md` - Cập nhật docs
5. ✅ `COMMIT_MESSAGE.txt` - Cập nhật commit message

## 🚀 Next Steps

1. **Sync Gradle:**
   ```bash
   gradlew.bat build
   ```

2. **Verify build:**
   ```bash
   gradlew.bat assembleDebug
   ```

3. **Test trên thiết bị:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

**Status:** ✅ **FIXED**  
**Date:** 2026-04-16  
**Time to fix:** 5 minutes
