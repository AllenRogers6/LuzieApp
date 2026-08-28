package com.allenrogers6.luzie.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch


import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import androidx.compose.material3.Text
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications

import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider

import androidx.compose.ui.unit.dp

import com.allenrogers6.luzie.core.AppInfo
import com.allenrogers6.luzie.core.getInstalledApps
import com.allenrogers6.luzie.data.AppPreferences
import androidx.compose.material3.ExperimentalMaterial3Api


//import com.allenrogers6.luzie.ui.settings.SettingViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkModeEnabled: Boolean,
    biometricsEnabled: Boolean,
    antiUninstallEnabled: Boolean,

    onBack: () -> Unit,
    onDarkMode: (Boolean) -> Unit,
    onNotifications: () -> Unit,
    onAccessibility: () -> Unit,
    onChangePin: () -> Unit,
    onBiometrics: (Boolean) -> Unit,
    onAntiUninstall: (Boolean) -> Unit,
    onAbout: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            contentPadding = PaddingValues(
                bottom = 16.dp,
            ),
        ) {

            item {
                SettingsSectionTitle(
                    text = "General",
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Change the theme of the app",
                    checked = darkModeEnabled,
                    onCheckedChange = onDarkMode,
                    onClick = {},
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Enable Luzie notifications",
                    onClick = onNotifications,
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Accessibility,
                    title = "Accessibility Service",
                    subtitle = "Manage Luzie's app monitoring service",
                    onClick = onAccessibility,
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            item {
                SettingsSectionTitle(
                    text = "Security",
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change PIN",
                    subtitle = "Change your Luzie PIN",
                    onClick = onChangePin,
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometrics",
                    subtitle = "Use fingerprint or face unlock",
                    checked = biometricsEnabled,
                    onCheckedChange = onBiometrics,
                    onClick = {},
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Anti-Uninstall",
                    subtitle = "Enable Luzie's anti-uninstall service",
                    checked = antiUninstallEnabled,
                    onCheckedChange = onAntiUninstall,
                    onClick = {},
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            item {
                SettingsSectionTitle(
                    text = "About",
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "About Luzie",
                    onClick = onAbout,
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (checked != null && onCheckedChange != null) {
                    onCheckedChange(!checked)
                } else {
                    onClick()
                }
            }
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
        )

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (checked != null && onCheckedChange != null) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}


@Composable
private fun SettingsSectionTitle(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
    )
}

