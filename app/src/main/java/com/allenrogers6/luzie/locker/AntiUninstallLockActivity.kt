package com.allenrogers6.luzie.locker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.allenrogers6.luzie.data.AppPreferences
import com.allenrogers6.luzie.data.Hasher
import com.allenrogers6.luzie.ui.theme.LuzieTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import  androidx.lifecycle.compose.collectAsStateWithLifecycle

class AntiUninstallLockActivity : ComponentActivity() {
    companion object {
        private const val TAG =
            "LuzieAntiUninstall"
    }

    private lateinit var preferences:
        AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
        )

        Log.d(
            TAG,
            "AntiUninstallLockActivity created",
        )

        preferences =
            AppPreferences(
                applicationContext,
            )

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    /*
                     * Do not allow the user to
                     * bypass authentication.
                     */
                }
            },
        )

        setContent {
            val darkModeEnabled by
                preferences
                    .isDarkMode
                    .collectAsStateWithLifecycle(
                        initialValue = false,
                    )

            LuzieTheme(
                darkTheme =
                darkModeEnabled,
            ) {
                AntiUninstallLockScreen(
                    onPinEntered = {
                        verifyPin(it)
                    },
                )
            }
        }
    }

    private fun verifyPin(pin: String) {
        lifecycleScope.launch {
            val expectedHash =
                preferences
                    .pinHash
                    .first()

            if (
                expectedHash != null &&
                Hasher.verify(
                    pin,
                    expectedHash,
                )
            ) {
                Log.d(
                    TAG,
                    "Anti-uninstall PIN accepted",
                )

                openDeviceAdminSettings()
            }
        }
    }

    private fun openDeviceAdminSettings() {
        val intent =
            Intent(
                Settings.ACTION_SECURITY_SETTINGS,
            )

        startActivity(intent)

        finish()

        overridePendingTransition(
            0,
            0,
        )
    }
}


@Composable
fun AntiUninstallLockScreen(
    onPinEntered: (String) -> Unit,
) {

    var pin by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {

        Text(
            text =
                "Anti-Uninstall Locked",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,
        )

        Spacer(
            modifier =
                Modifier.height(12.dp),
        )

        Text(
            text =
                "Enter your Luzie PIN to manage Device Admin.",
        )

        Spacer(
            modifier =
                Modifier.height(24.dp),
        )

        OutlinedTextField(

            value = pin,

            onValueChange = { value ->

                if (
                    value.length <= 4 &&
                    value.all(
                        Char::isDigit,
                    )
                ) {

                    pin = value
                }
            },

            label = {
                Text("Luzie PIN")
            },

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.NumberPassword,
                ),

            singleLine = true,
        )

        Spacer(
            modifier =
                Modifier.height(16.dp),
        )

        Button(

            enabled =
                pin.length == 4,

            onClick = {
                onPinEntered(pin)
            },
        ) {

            Text("Continue")
        }
    }
}
