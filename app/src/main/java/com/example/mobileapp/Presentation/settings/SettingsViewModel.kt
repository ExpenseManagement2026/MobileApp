package com.example.mobileapp.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.di.RepositoryProvider
import com.example.mobileapp.data.local.database.AppDatabase
import com.example.mobileapp.presentation.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val isDarkMode: Boolean = false,
    val showResetDialog: Boolean = false,
    val resetSuccess: Boolean = false,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePrefs = ThemePreferences(application)
    private val db = AppDatabase.getDatabase(application)

    private val _state = MutableStateFlow(
        SettingsState(isDarkMode = themePrefs.isDarkMode())
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        themePrefs.setDarkMode(enabled)
        _state.value = _state.value.copy(isDarkMode = enabled)
    }

    fun showResetDialog() {
        _state.value = _state.value.copy(showResetDialog = true)
    }

    fun dismissResetDialog() {
        _state.value = _state.value.copy(showResetDialog = false)
    }

    fun resetData() {
        viewModelScope.launch {
            db.transactionDao().deleteAllTransactions()
            RepositoryProvider.resetRepository()
            _state.value = _state.value.copy(showResetDialog = false, resetSuccess = true)
        }
    }

    fun clearResetSuccess() {
        _state.value = _state.value.copy(resetSuccess = false)
    }
}
