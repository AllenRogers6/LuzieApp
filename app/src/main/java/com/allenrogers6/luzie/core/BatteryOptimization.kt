package com.allenrogers6.luzie.locker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

fun openLuzieBatterySettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return
    }

    val intent =
        Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        ).apply {
            data =
                Uri.parse(
                    "package:${context.packageName}",
                )
        }

    context.startActivity(intent)
}
