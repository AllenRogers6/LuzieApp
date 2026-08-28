package com.allenrogers6.luzie.locker

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.allenrogers6.luzie.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LuzieDeviceAdminReceiver : DeviceAdminReceiver() {
    companion object {
        private const val TAG = "LuzieDeviceAdmin"
    }

    override fun onEnabled(
        context: Context,
        intent: Intent,
    ) {
        super.onEnabled(context, intent)

        Log.d(
            TAG,
            "Device Admin enabled",
        )

        val preferences =
            AppPreferences(
                context.applicationContext,
            )

        CoroutineScope(
            Dispatchers.IO,
        ).launch {
            preferences.setAntiUninstall(
                true,
            )
        }
    }

    override fun onDisableRequested(
        context: Context,
        intent: Intent,
    ): CharSequence {
        Log.d(
            TAG,
            "Device Admin disable requested",
        )

        return "Luzie Anti-Uninstall is enabled. Disable Anti-Uninstall from Luzie before removing protection."
    }

    override fun onDisabled(
        context: Context,
        intent: Intent,
    ) {
        super.onDisabled(
            context,
            intent,
        )

        Log.d(
            TAG,
            "Device Admin disabled",
        )

        val preferences =
            AppPreferences(
                context.applicationContext,
            )

        CoroutineScope(
            Dispatchers.IO,
        ).launch {
            preferences.setAntiUninstall(
                false,
            )
        }
    }
}
