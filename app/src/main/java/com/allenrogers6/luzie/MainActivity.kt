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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.activity.result.contract.ActivityResultContracts


import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable

import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope

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
import com.allenrogers6.luzie.locker.LuzieDeviceAdminReceiver




class MainActivity : ComponentActivity() {
    private var deviceAdminActive by mutableStateOf(false)

    private val deviceAdminLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {

                val dpm = getSystemService(DevicePolicyManager::class.java)

                val admin = ComponentName(
                    this,
                    LuzieDeviceAdminReceiver::class.java,
                )

                if (dpm.isAdminActive(admin)) {
                    lifecycleScope.launch {
                        AppPreferences(this@MainActivity)
                            .setAntiUninstall(true)
                    }
                }
            }

      private fun updateDeviceAdminState() {
          val dpm =
              getSystemService(
                  Context.DEVICE_POLICY_SERVICE
              ) as DevicePolicyManager

          val admin = ComponentName(
              this,
              LuzieDeviceAdminReceiver::class.java,
          )

          val active = dpm.isAdminActive(admin)

          deviceAdminActive = active

          if (!active) {
              lifecycleScope.launch {
                  AppPreferences(this@MainActivity)
                      .setAntiUninstall(false)
              }
          }
      }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateDeviceAdminState()
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
                            onRequestDeviceAdmin = { intent -> deviceAdminLauncher.launch(intent) },
                            deviceAdminActive = deviceAdminActive,
                        )
                    }

                    true -> {
                        LuzieApp(
                            preferences = preferences,
                            startDestination = "home",
                            onRequestDeviceAdmin = { intent -> deviceAdminLauncher.launch(intent) },

                            deviceAdminActive = deviceAdminActive,
                        )
                    }
                }
            }
        }
    }
    override fun onResume() {
            super.onResume()
            updateDeviceAdminState()
    }
}


@Composable
fun LuzieApp(
    preferences: AppPreferences,
    startDestination: String,
    onRequestDeviceAdmin: (Intent) -> Unit,
    deviceAdminActive: Boolean,
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

            val antiUninstallPreference by
                preferences
                    .isAntiUninstall
                    .collectAsStateWithLifecycle(
                        initialValue = false
                    )

            val antiUninstallEnabled =
                antiUninstallPreference && deviceAdminActive
          

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
                  if (enabled) {
                      val dpm = context.getSystemService(DevicePolicyManager::class.java)

                      val admin = ComponentName(
                          context,
                          LuzieDeviceAdminReceiver::class.java,
                      )

                      if (dpm.isAdminActive(admin)) {
                          scope.launch {
                              preferences.setAntiUninstall(true)
                          }
                      } else {
                          val intent = Intent(
                              DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN,
                          ).apply {
                              putExtra(
                                  DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                  admin,
                              )

                              putExtra(
                                  DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                  "Luzie anti-uninstall requires Device Admin protection.",
                              )
                          }
                          onRequestDeviceAdmin(intent)
                      }
                  } else {
                      scope.launch {
                          preferences.setAntiUninstall(false)
                      }
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
