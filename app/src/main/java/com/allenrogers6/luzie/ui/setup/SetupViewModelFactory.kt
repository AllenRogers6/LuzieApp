package com.allenrogers6.luzie.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.allenrogers6.luzie.data.AppPreferences

class SetupViewModelFactory(
    private val preferences: AppPreferences,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                SetupViewModel::class.java
            )
        ) {
            return SetupViewModel(
                preferences
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
