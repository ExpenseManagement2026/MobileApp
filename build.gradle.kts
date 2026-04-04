// File build.gradle.kts (Project: MobileApp)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false // Tên ngắn gọn theo .toml mới
}