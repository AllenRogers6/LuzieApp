package com.allenrogers6.luzie.ui.setup

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect

import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

import androidx.compose.material3.MaterialTheme

import com.allenrogers6.luzie.locker.AppMonitorService

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager

import android.net.Uri
import android.util.Log

import com.allenrogers6.luzie.R


private const val TAG = "LuzieSetup"

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onSetupComplete: () -> Unit,
) {
    val context = LocalContext.current

    var pin by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPin by rememberSaveable {
        mutableStateOf("")
    }

    var accessibilityEnabled by remember {
        mutableStateOf(false)
    }

    var unrestrictedBattery by remember {
        mutableStateOf(false)
    }

    fun refreshRequirements() {
        accessibilityEnabled =
            isAccessibilityEnabled(context)

        unrestrictedBattery =
            isUnrestrictedBattery(context)
    }

    val pinValid =
        pin.length == 4 &&
            pin.all(Char::isDigit)

    val pinsMatch =
        pin == confirmPin

    val ready =
        pinValid &&
            pinsMatch &&
            accessibilityEnabled &&
            unrestrictedBattery

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {
                    refreshRequirements()
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        refreshRequirements()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Bottom,
    ) {

        Text(
            text = "Hi,",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Lets help you set up Luzie.",
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
                Text("Create PIN")
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.NumberPassword,
                ),
            singleLine = true,
        )

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { value ->
                if (
                    value.length <= 4 &&
                    value.all(Char::isDigit)
                ) {
                    confirmPin = value
                }
            },
            label = {
                Text("Confirm PIN")
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.NumberPassword,
                ),
            singleLine = true,
        )

        Text(
            text =
                if (pinsMatch) {
                    "PINs match"
                } else {
                    "PINs do not match"
                },
        )

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS,
                    ),
                )
            },
        ) {
            Text(
                if (accessibilityEnabled) {
                    "Accessibility Service Enabled"
                } else {
                    "Enable Luzie Accessibility"
                },
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (!unrestrictedBattery) {
                    val intent =
                        Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        ).apply {
                            data =
                                android.net.Uri.parse(
                                    "package:${context.packageName}",
                                )
                        }

                    context.startActivity(intent)
                }
            },
        ) {
            Text(
                if (unrestrictedBattery) {
                    "Background Usage Unrestricted"
                } else {
                    "Allow Unrestricted Background Usage"
                },
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = ready,
            onClick = {
                viewModel.completeSetup(
                    pin = pin,
                    onComplete = onSetupComplete,
                )
            },
        ) {
            Text("Done!")
        }
    }
}


fun isAccessibilityEnabled(
    context: Context,
): Boolean {
    val accessibilityManager =
        context.getSystemService(
            Context.ACCESSIBILITY_SERVICE,
        ) as android.view.accessibility.AccessibilityManager

    val enabledServices =
        accessibilityManager
            .getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
            )

    return enabledServices.any { service ->
        val serviceInfo =
            service.resolveInfo.serviceInfo

        serviceInfo.packageName ==
            context.packageName &&
        serviceInfo.name ==
            AppMonitorService::class.java.name
    }
}

fun isUnrestrictedBattery(
    context: Context,
): Boolean {

    if (
        Build.VERSION.SDK_INT <
        Build.VERSION_CODES.M
    ) {
        return true
    }

    val powerManager =
        context.getSystemService(
            Context.POWER_SERVICE,
        ) as PowerManager

    return powerManager.isIgnoringBatteryOptimizations(
        context.packageName,
    )
}
