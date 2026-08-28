package com.allenrogers6.luzie.locker

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.allenrogers6.luzie.data.AppPreferences
import com.allenrogers6.luzie.data.Hasher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface


import com.allenrogers6.luzie.ui.theme.LuzieTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class AppLock : FragmentActivity() {

    companion object {
        const val EXTRA_LOCKED_PACKAGE =
            "locked_package"

        private const val TAG =
            "LuzieAppLock"
    }

    private lateinit var preferences: AppPreferences

    private var lockedPackage: String? = null

    private var biometricPrompt: BiometricPrompt? = null

    private var biometricAttempted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "AppLock onCreate")

        preferences =
            AppPreferences(
                applicationContext,
            )

        lockedPackage =
            intent.getStringExtra(
                EXTRA_LOCKED_PACKAGE,
            )

        Log.d(
            TAG,
            "Locked package: $lockedPackage",
        )

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(
                        TAG,
                        "Back pressed - blocked",
                    )
                }
            },
        )

        setContent {
            val darkModeEnabled by preferences
                .isDarkMode
                .collectAsStateWithLifecycle(
                    initialValue = false,
                )

            LuzieTheme(
                darkTheme = darkModeEnabled,
            ) {
                LockScreen(
                    onPinEntered = { pin ->
                        verifyPin(pin)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "AppLock onResume")

        if (!biometricAttempted) {
            biometricAttempted = true

            lifecycleScope.launch {
                checkBiometrics()
            }
        }
    }

    override fun onNewIntent(
        intent: android.content.Intent,
    ) {
        super.onNewIntent(intent)

        Log.d(
            TAG,
            "AppLock onNewIntent",
        )

        lockedPackage =
            intent.getStringExtra(
                EXTRA_LOCKED_PACKAGE,
            )

        biometricAttempted = false
    }

    private suspend fun checkBiometrics() {

        val biometricEnabled =
            preferences
                .isBiometricsEnabled
                .first()

        Log.d(
            TAG,
            "Biometrics enabled: $biometricEnabled",
        )

        if (!biometricEnabled) {
            return
        }

        authenticateWithBiometrics()
    }

    private fun authenticateWithBiometrics() {

        Log.d(
            TAG,
            "STEP 1: authenticateWithBiometrics entered",
        )

        val biometricManager =
            BiometricManager.from(this)

        Log.d(
            TAG,
            "STEP 2: BiometricManager created",
        )

        val authenticators =
            BiometricManager.Authenticators
                .BIOMETRIC_WEAK

        val result =
            biometricManager.canAuthenticate(
                authenticators,
            )

        Log.d(
            TAG,
            "STEP 3: canAuthenticate = $result",
        )

        if (
            result !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            Log.w(
                TAG,
                "STEP 4: Biometrics unavailable",
            )
            return
        }

        val executor =
            ContextCompat.getMainExecutor(this)

        biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result:
                            BiometricPrompt.AuthenticationResult,
                    ) {
                        super.onAuthenticationSucceeded(
                            result,
                        )

                        Log.d(
                            TAG,
                            "BIOMETRIC SUCCESS",
                        )

                        unlock()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        super.onAuthenticationError(
                            errorCode,
                            errString,
                        )

                        Log.e(
                            TAG,
                            "BIOMETRIC ERROR: " +
                                "$errorCode - $errString",
                        )
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()

                        Log.w(
                            TAG,
                            "BIOMETRIC FAILED",
                        )
                    }
                },
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Luzie")
                .setSubtitle(
                    "Authenticate to continue",
                )
                .setNegativeButtonText(
                    "Use PIN",
                )
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators
                        .BIOMETRIC_WEAK,
                )
                .build()

        try {
            biometricPrompt?.authenticate(
                promptInfo,
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "BIOMETRIC PROMPT EXCEPTION",
                e,
            )
        }
    }

    private fun verifyPin(
        pin: String,
    ) {
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
                    "PIN authentication succeeded",
                )

                unlock()
            } else {
                Log.d(
                    TAG,
                    "PIN authentication failed",
                )
            }
        }
    }

    private fun unlock() {

        val packageName =
            lockedPackage
                ?: run {
                    Log.e(
                        TAG,
                        "Cannot unlock: no locked package",
                    )
                    return
                }

        lifecycleScope.launch {

            Log.d(
                TAG,
                "Temporarily unlocking: $packageName",
            )

            preferences.setTemporarilyUnlocked(
                packageName,
            )

            finish()

            overridePendingTransition(
                0,
                0,
            )
        }
    }

    override fun onDestroy() {
        Log.d(
            TAG,
            "AppLock onDestroy",
        )

        biometricPrompt = null

        super.onDestroy()
    }
}



@Composable
fun LockScreen(
    onPinEntered: (String) -> Unit,
) {
    var pin by rememberSaveable {
        mutableStateOf("")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center,
        ) {

            Text(
                text = "App Locked",
                style =
                    MaterialTheme.typography.headlineMedium,
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { value ->

                    if (
                        value.length <= 4 &&
                        value.all(Char::isDigit)
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
                modifier = Modifier.height(16.dp)
            )

            Button(
                enabled = pin.length == 4,
                onClick = {
                    onPinEntered(pin)
                },
            ) {
                Text("Unlock")
            }
        }
    }
}
