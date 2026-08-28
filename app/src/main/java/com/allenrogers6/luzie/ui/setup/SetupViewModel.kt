package com.allenrogers6.luzie.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allenrogers6.luzie.data.AppPreferences
import com.allenrogers6.luzie.data.Hasher
import kotlinx.coroutines.launch

class SetupViewModel(
    private val preferences: AppPreferences,
) : ViewModel() {
    fun completeSetup(
        pin: String,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            preferences.setPinHash(
                Hasher.hash(pin),
            )

            preferences.setSetupComplete(true)

            onComplete()
        }
    }
}
