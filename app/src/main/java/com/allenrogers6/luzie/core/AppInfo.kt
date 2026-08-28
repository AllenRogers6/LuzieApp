package com.allenrogers6.luzie.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val isLocked: Boolean,
)

fun getInstalledApps(context: Context): List<AppInfo> {
    val packageManager = context.packageManager

    return packageManager
        .getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { appInfo ->
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
        }.map { appInfo ->
            AppInfo(
                name = appInfo.loadLabel(packageManager).toString(),
                packageName = appInfo.packageName,
                icon = appInfo.loadIcon(packageManager),
                isLocked = false,
            )
        }.sortedBy { it.name.lowercase() }
}
