package com.allenrogers6.luzie.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

private const val DATASTORE_NAME =
    "luzie_preferences"

private val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME,
)

private object PreferencesKeys {
    val SETUP_COMPLETE =
        booleanPreferencesKey("setup_complete")

    val PIN_HASH =
        stringPreferencesKey("pin_hash")

    val LOCKED_PACKAGES =
        stringSetPreferencesKey("locked_packages")

    val TEMPORARILY_UNLOCKED_PACKAGE =
        stringPreferencesKey(
            "temporarily_unlocked_package",
        )

    val IS_NOTIFICATIONS_ENABLED =
        booleanPreferencesKey("notifications_enabled")

    val IS_DARK_MODE =
        booleanPreferencesKey("dark_mode")

    val IS_ANTI_UNINSTALL =
        booleanPreferencesKey("anti_uninstall")

    val IS_BIOMETRICS_ENABLED =
        booleanPreferencesKey("biometrics_enabled")
}

class AppPreferences(
    private val context: Context,
) {
    val setupComplete: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[
                PreferencesKeys.SETUP_COMPLETE,
            ] ?: false
        }

    val pinHash: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[
                PreferencesKeys.PIN_HASH,
            ]
        }

    val lockedPackages: Flow<Set<String>> =
        context.dataStore.data.map { preferences ->
            preferences[
                PreferencesKeys.LOCKED_PACKAGES,
            ] ?: emptySet()
        }

    val temporarilyUnlockedPackage: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[
                PreferencesKeys.TEMPORARILY_UNLOCKED_PACKAGE,
            ]
        }

    val isNotificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_NOTIFICATIONS_ENABLED] ?: false
        }

    val isDarkMode: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false
        }

    val isAntiUninstall: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_ANTI_UNINSTALL] ?: false
        }

    val isBiometricsEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRICS_ENABLED] ?: false
        }

    suspend fun setSetupComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[
                PreferencesKeys.SETUP_COMPLETE,
            ] = complete
        }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { preferences ->
            preferences[
                PreferencesKeys.PIN_HASH,
            ] = hash
        }
    }

    suspend fun setAppLocked(
        packageName: String,
        locked: Boolean,
    ) {
        context.dataStore.edit { preferences ->

            val current =
                preferences[
                    PreferencesKeys.LOCKED_PACKAGES,
                ] ?: emptySet()

            preferences[
                PreferencesKeys.LOCKED_PACKAGES,
            ] =
                if (locked) {
                    current + packageName
                } else {
                    current - packageName
                }
        }
    }

    suspend fun setTemporarilyUnlocked(packageName: String?) {
        context.dataStore.edit { preferences ->

            if (packageName == null) {
                preferences.remove(
                    PreferencesKeys.TEMPORARILY_UNLOCKED_PACKAGE,
                )
            } else {
                preferences[
                    PreferencesKeys.TEMPORARILY_UNLOCKED_PACKAGE,
                ] = packageName
            }
        }
    }

    suspend fun clearTemporarilyUnlocked() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TEMPORARILY_UNLOCKED_PACKAGE)
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }

    suspend fun setAntiUninstall(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ANTI_UNINSTALL] = enabled
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRICS_ENABLED] = enabled
        }
    }
}
