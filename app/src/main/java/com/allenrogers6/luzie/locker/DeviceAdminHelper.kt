package com.allenrogers6.luzie.locker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

fun getLuzieAdminComponent(context: Context): ComponentName =
    ComponentName(
        context,
        LuzieDeviceAdminReceiver::class.java,
    )

fun isLuzieDeviceAdmin(context: Context): Boolean {
    val devicePolicyManager =
        context.getSystemService(
            Context.DEVICE_POLICY_SERVICE,
        ) as DevicePolicyManager

    return devicePolicyManager.isAdminActive(
        getLuzieAdminComponent(context),
    )
}

fun requestLuzieDeviceAdmin(context: Context) {
    val component =
        getLuzieAdminComponent(context)

    val intent =
        Intent(
            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN,
        ).apply {
            putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                component,
            )

            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Luzie uses device administration to prevent itself from being uninstalled while Anti-Uninstall is enabled.",
            )
        }

    context.startActivity(intent)
}
