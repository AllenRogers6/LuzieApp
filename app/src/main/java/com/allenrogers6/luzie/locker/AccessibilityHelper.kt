package com.allenrogers6.luzie.locker

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

fun isAccessibilityServiceEnabled(
    context: Context,
): Boolean {

    val expectedComponent =
        ComponentName(
            context,
            AppMonitorService::class.java,
        )

    val enabledServices =
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

    return enabledServices
        .split(':')
        .any { service ->
            ComponentName.unflattenFromString(service) ==
                expectedComponent
        }
}
