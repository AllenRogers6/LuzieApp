package com.allenrogers6.luzie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenrogers6.luzie.data.AppPreferences
import com.allenrogers6.luzie.locker.isAccessibilityServiceEnabled
import com.allenrogers6.luzie.ui.home.HomeScreen
import com.allenrogers6.luzie.ui.home.HomeViewModel
import com.allenrogers6.luzie.ui.setup.SetupScreen
import com.allenrogers6.luzie.ui.setup.SetupViewModel
import com.allenrogers6.luzie.ui.setup.SetupViewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.allenrogers6.luzie.ui.home.HomeViewModelFactory
import android.content.Context

import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable

import androidx.compose.material3.Text

import com.allenrogers6.luzie.ui.theme.LuzieTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.provider.Settings
import androidx.activity.enableEdgeToEdge

import com.allenrogers6.luzie.ui.settings.SettingsScreen
import com.allenrogers6.luzie.ui.settings.PinChangeScreen
import com.allenrogers6.luzie.ui.settings.AboutScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current

            val preferences = remember {
                AppPreferences(context)
            }

            val setupComplete by preferences
                .setupComplete
                .collectAsStateWithLifecycle(initialValue = null)

            val darkModeEnabled by preferences
                .isDarkMode
                .collectAsStateWithLifecycle(
                    initialValue = false,
                )

            LuzieTheme(
                darkTheme = darkModeEnabled,
            ) {
                when (setupComplete) {

                    null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    false -> {
                        LuzieApp(
                            preferences = preferences,
                            startDestination = "setup",
                        )
                    }

                    true -> {
                        LuzieApp(
                            preferences = preferences,
                            startDestination = "home",
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun LuzieApp(
    preferences: AppPreferences,
    startDestination: String,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        composable("setup") {

            val setupViewModel: SetupViewModel = viewModel(
                factory = SetupViewModelFactory(
                    preferences
                )
            )

            SetupScreen(
                viewModel = setupViewModel,
                onSetupComplete = {
                    navController.navigate("home") {
                        popUpTo("setup") {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable("home") {

            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    preferences
                )
            )

            HomeScreen(
                viewModel = homeViewModel,
            onSettings = {
                  navController.navigate("settings")
              },
                  )
        }
        composable("settings") {
          val scope = rememberCoroutineScope()
          val darkModeEnabled by preferences
            .isDarkMode
            .collectAsStateWithLifecycle(
                initialValue = false,
            )
          val biometricEnabled by preferences
              .isBiometricsEnabled
              .collectAsStateWithLifecycle(
                  initialValue = false,
              )

          val antiUninstallEnabled by
                preferences
                    .isAntiUninstall
                    .collectAsStateWithLifecycle(
                        initialValue = false
                    ) 
          val context = LocalContext.current

          SettingsScreen(

              darkModeEnabled = darkModeEnabled,
              biometricsEnabled = biometricEnabled,
              antiUninstallEnabled = antiUninstallEnabled,

              onBack = {
                  navController.popBackStack()
              },
              onDarkMode = { enabled ->
                  scope.launch {
                      preferences.setDarkMode(enabled)
                  }
              },
              onNotifications = {
                openNotificationSettings(context)
              },

              
              onAccessibility = {
                openAccessibilitySettings(context)
              },
              onChangePin = {
                  navController.navigate("change_pin")
              },
              onBiometrics = { enabled ->
                scope.launch {
                    preferences.setBiometricsEnabled(enabled)
                }
              },
              onAntiUninstall = { enabled ->
                scope.launch {

                        preferences
                            .setAntiUninstall(
                                enabled
                            )
                    }
              },
              onAbout = {
                  navController.navigate("about")
              },
          )
      }
        composable("change_pin") {
          PinChangeScreen( preferences = preferences, onBack = { navController.popBackStack() })
        }


        composable("about") {
          AboutScreen( onBack = { navController.popBackStack() })
        }


    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}


private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_ACCESSIBILITY_SETTINGS
    )

    context.startActivity(intent)
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APP_NOTIFICATION_SETTINGS,
    ).apply {
        putExtra(
            Settings.EXTRA_APP_PACKAGE,
            context.packageName,
        )
    }

    context.startActivity(intent)
}
