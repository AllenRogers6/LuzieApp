/*package com.allenrogers6.luzie.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allenrogers6.luzie.data.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        repository
            .getDataStore()
            .map { preferences ->
                SettingsUiState(
                    isDarkMode = preferences[IS_DARK_MODE] ?: false,
                    userName = preferences[USER_NAME] ?: "",
                    notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsUiState(),
            )

    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentState = uiState.value
            repository.setDarkMode(!currentState.isDarkMode)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.setUserName(name)
        }
    }
}

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val userName: String = "",
    val notificationsEnabled: Boolean = true,
)*/
