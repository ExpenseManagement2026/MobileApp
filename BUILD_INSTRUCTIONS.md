# 🔨 Hướng dẫn Build - Chức năng Scan Hóa Đơn

## ✅ Prerequisites

- ✅ Android Studio (latest version)
- ✅ JDK 17
- ✅ Android SDK 24+
- ✅ Gradle 8.0+

## 📦 Dependencies đã thêm

Tất cả dependencies đã được thêm vào `app/build.gradle.kts`:

```kotlin
// CameraX
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// ML Kit Text Recognition
implementation("com.google.mlkit:text-recognition:16.0.0")

// Kotlin Coroutines Play Services
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## 🔧 Build Steps

### 1. Sync Gradle

**Windows:**
```bash
gradlew.bat --refresh-dependencies
```

**Mac/Linux:**
```bash
./gradlew --refresh-dependencies
```

### 2. Clean Build

**Windows:**
```bash
gradlew.bat clean
```

**Mac/Linux:**
```bash
./gradlew clean
```

### 3. Build Debug APK

**Windows:**
```bash
gradlew.bat assembleDebug
```

**Mac/Linux:**
```bash
./gradlew assembleDebug
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK

**Windows:**
```bash
gradlew.bat assembleRelease
```

**Mac/Linux:**
```bash
./gradlew assembleRelease
```

**Output:** `app/build/outputs/apk/release/app-release.apk`

## 📱 Install & Run

### Option 1: Via Android Studio

1. Open project in Android Studio
2. Wait for Gradle sync
3. Click **Run** (Shift+F10)
4. Select device/emulator
5. App will install and launch

### Option 2: Via ADB

**Install Debug APK:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Install Release APK:**
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

**Reinstall (if already installed):**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Manual Install

1. Copy APK to device
2. Open file manager on device
3. Tap APK file
4. Allow "Install from unknown sources" if prompted
5. Tap "Install"

## 🧪 Verify Installation

### 1. Check app is installed
```bash
adb shell pm list packages | grep com.example.mobileapp
```

### 2. Launch app
```bash
adb shell am start -n com.example.mobileapp/.MainActivity
```

### 3. Check logs
```bash
adb logcat | grep MobileApp
```

## 🐛 Troubleshooting

### Issue 1: Gradle sync failed

**Solution:**
```bash
# Clear Gradle cache
gradlew.bat --stop
rm -rf .gradle
gradlew.bat clean build
```

### Issue 2: Dependency resolution failed

**Solution:**
```bash
# Refresh dependencies
gradlew.bat --refresh-dependencies
```

### Issue 3: Build failed with "Unresolved reference"

**Solution:**
- Ensure all dependencies are in `build.gradle.kts`
- Sync Gradle again
- Invalidate caches: File > Invalidate Caches > Invalidate and Restart

### Issue 4: APK not found

**Solution:**
```bash
# Check build output
ls -la app/build/outputs/apk/debug/
```

### Issue 5: Installation failed

**Solution:**
```bash
# Uninstall old version first
adb uninstall com.example.mobileapp

# Then install new version
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📊 Build Variants

### Debug Build
- **Debuggable:** Yes
- **Minified:** No
- **Obfuscated:** No
- **Size:** ~15-20 MB
- **Use case:** Development, testing

### Release Build
- **Debuggable:** No
- **Minified:** Yes (if enabled)
- **Obfuscated:** Yes (if enabled)
- **Size:** ~10-15 MB
- **Use case:** Production, distribution

## 🔐 Signing (Release Build)

### Generate Keystore

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### Configure in `build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("my-release-key.jks")
            storePassword = "your-store-password"
            keyAlias = "my-key-alias"
            keyPassword = "your-key-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

### Build Signed APK

```bash
gradlew.bat assembleRelease
```

## 📦 Build Output

### Debug Build
```
app/build/outputs/apk/debug/
├── app-debug.apk           # Main APK
└── output-metadata.json    # Build metadata
```

### Release Build
```
app/build/outputs/apk/release/
├── app-release.apk         # Main APK (unsigned)
├── app-release-unsigned.apk
└── output-metadata.json
```

## 🚀 CI/CD Integration

### GitHub Actions Example

```yaml
name: Build APK

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## 📊 Build Performance

### Typical Build Times

| Build Type | Clean Build | Incremental Build |
|------------|-------------|-------------------|
| Debug      | 2-3 min     | 30-60 sec        |
| Release    | 3-5 min     | 1-2 min          |

### Optimization Tips

1. **Enable Gradle Daemon:**
   ```properties
   # gradle.properties
   org.gradle.daemon=true
   ```

2. **Increase Heap Size:**
   ```properties
   # gradle.properties
   org.gradle.jvmargs=-Xmx4096m
   ```

3. **Enable Parallel Execution:**
   ```properties
   # gradle.properties
   org.gradle.parallel=true
   ```

4. **Enable Build Cache:**
   ```properties
   # gradle.properties
   org.gradle.caching=true
   ```

## ✅ Verification Checklist

Before releasing, verify:

- [ ] App builds successfully
- [ ] No compilation errors
- [ ] No lint errors (critical)
- [ ] All permissions declared
- [ ] Camera permission works
- [ ] Scan feature works
- [ ] Auto-fill works
- [ ] No crashes on startup
- [ ] No memory leaks
- [ ] Performance acceptable

## 📞 Support

### Build Issues
- Check [BUGFIX_COROUTINES_DEPENDENCY.md](./BUGFIX_COROUTINES_DEPENDENCY.md)
- Check Gradle logs: `gradlew.bat build --stacktrace`
- Check Android Studio Build Output

### Runtime Issues
- Check logcat: `adb logcat`
- Check crash reports
- Check [SCAN_FEATURE_TEST_PLAN.md](./SCAN_FEATURE_TEST_PLAN.md)

---

## 🎯 Quick Commands Reference

```bash
# Clean
gradlew.bat clean

# Build Debug
gradlew.bat assembleDebug

# Build Release
gradlew.bat assembleRelease

# Install Debug
adb install app/build/outputs/apk/debug/app-debug.apk

# Uninstall
adb uninstall com.example.mobileapp

# Launch
adb shell am start -n com.example.mobileapp/.MainActivity

# Logs
adb logcat | grep MobileApp
```

---

**Last Updated:** 2026-04-16  
**Version:** 1.0.0  
**Status:** ✅ Ready to Build
