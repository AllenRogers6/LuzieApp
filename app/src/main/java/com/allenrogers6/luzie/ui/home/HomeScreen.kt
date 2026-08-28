package com.allenrogers6.luzie.ui.home

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
import androidx.compose.material3.Surface

import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Settings

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import androidx.compose.material3.Text
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap

import com.allenrogers6.luzie.core.AppInfo
import com.allenrogers6.luzie.core.getInstalledApps
import com.allenrogers6.luzie.data.AppPreferences


class HomeViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel() {

    var apps by mutableStateOf<List<AppInfo>>(emptyList())
        private set

    fun loadApps(context: Context) {
        if (apps.isNotEmpty()) return

        viewModelScope.launch {
            val lockedPackages =
                appPreferences.lockedPackages.first()

            apps = getInstalledApps(context).map { app ->
                app.copy(
                    isLocked = app.packageName in lockedPackages
                )
            }
        }
    }

    fun toggleLock(packageName: String) {
        val app = apps.firstOrNull {
            it.packageName == packageName
        } ?: return

        val newLockedState = !app.isLocked

        apps = apps.map {
            if (it.packageName == packageName) {
                it.copy(isLocked = newLockedState)
            } else {
                it
            }
        }

        viewModelScope.launch {
            appPreferences.setAppLocked(
                packageName = packageName,
                locked = newLockedState,
            )
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {
                    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return HomeViewModel(
                            AppPreferences(context.applicationContext)
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class"
                    )
                }
            }
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    onClick = onSettings,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = viewModel.apps,
                    key = { app -> app.packageName },
                ) { app ->

                    AppItem(
                        app = app,
                        onToggleLock = {
                            viewModel.toggleLock(
                                app.packageName
                            )
                        },
                    )
                }
            }
        }
    }
}


@Composable
fun AppItem(
    app: AppInfo,
    onToggleLock: () -> Unit,
) {
    val bitmap = remember(app.packageName) {
        app.icon.toBitmap().asImageBitmap()
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = app.name,
            modifier = Modifier.size(48.dp),
        )

        Text(
            text = app.name,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconButton(
          onClick = onToggleLock,
        ) {
            Icon(
                imageVector = if (app.isLocked) {
                    Icons.Default.Lock
                } else {
                    Icons.Default.LockOpen
                },
                contentDescription = if (app.isLocked) {
                    "Locked"
                } else {
                    "Unlocked"
                },
            )
        }
    }
}
