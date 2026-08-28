package com.allenrogers6.luzie.locker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.allenrogers6.luzie.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppMonitorService : AccessibilityService() {
    companion object {
        private const val TAG = "LuzieLocker"
        const val SETTINGS_PACKAGE = "com.android.settings"
    }

    private lateinit var preferences: AppPreferences

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.IO,
        )

    private var lockScreenShowing = false

    private var antiUninstallScreenDetected =
        false

    override fun onServiceConnected() {
        super.onServiceConnected()

        preferences =
            AppPreferences(applicationContext)

        Log.d(
            TAG,
            "Accessibility service connected",
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            return
        }

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            return
        }

        val packageName =
            event.packageName
                ?.toString()
                ?: return

        if (
            packageName ==
            applicationContext.packageName
        ) {
            return
        }

        if (
            packageName == SETTINGS_PACKAGE
        ) {
            handleDeviceAdminSettings()
            return
        }

        antiUninstallScreenDetected = false

        serviceScope.launch {
            val lockedPackages =
                preferences
                    .lockedPackages
                    .first()

            val temporarilyUnlocked =
                preferences
                    .temporarilyUnlockedPackage
                    .first()

            if (
                temporarilyUnlocked != null &&
                packageName != temporarilyUnlocked
            ) {
                preferences
                    .clearTemporarilyUnlocked()
            }

            if (
                packageName == temporarilyUnlocked
            ) {
                return@launch
            }

            if (
                packageName in lockedPackages
            ) {
                showLockScreen(packageName)
            }
        }
    }

    private fun showLockScreen(packageName: String) {
        val intent =
            Intent(
                this,
                AppLock::class.java,
            ).apply {
                putExtra(
                    AppLock.EXTRA_LOCKED_PACKAGE,
                    packageName,
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION,
                )
            }

        startActivity(intent)
    }

    private fun handleDeviceAdminSettings() {
        if (
            antiUninstallScreenDetected
        ) {
            return
        }

        CoroutineScope(
            Dispatchers.Main,
        ).launch {
            val enabled =
                preferences
                    .isAntiUninstall
                    .first()

            if (!enabled) {
                return@launch
            }

            if (
                !isLuzieDeviceAdmin(
                    applicationContext,
                )
            ) {
                return@launch
            }

            if (
                !isDeviceAdminDeactivationScreen()
            ) {
                return@launch
            }

            antiUninstallScreenDetected =
                true

            Log.d(
                TAG,
                "Blocking Device Admin deactivation flow",
            )

            performGlobalAction(
                GLOBAL_ACTION_BACK,
            )

            kotlinx.coroutines.delay(150)

            launchAntiUninstallLock()
        }
    }

    private fun isAntiUninstallEnabled(): Boolean =
        try {
            kotlinx.coroutines.runBlocking {
                preferences
                    .isAntiUninstall
                    .first()
            }
        } catch (
            exception: Exception,
        ) {
            Log.e(
                TAG,
                "Unable to read anti-uninstall preference",
                exception,
            )

            false
        }

    private fun isDeviceAdminDeactivationScreen(): Boolean {
        val root =
            rootInActiveWindow
                ?: return false

        return containsAnyText(
            root,
            listOf(
                "Deactivate",
                "Deactivate this device admin app",
                "Turn off",
                "Disable",
                "device administrator",
            ),
        )
    }

    private fun containsAnyText(
        root: AccessibilityNodeInfo,
        texts: List<String>,
    ): Boolean {
        for (text in texts) {
            val nodes =
                root.findAccessibilityNodeInfosByText(
                    text,
                )

            if (
                nodes.isNotEmpty()
            ) {
                return true
            }
        }

        return false
    }

    private fun launchAntiUninstallLock() {
        Log.d(
            TAG,
            "Launching AntiUninstallLockActivity",
        )

        val intent =
            Intent(
                this,
                AntiUninstallLockActivity::class.java,
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
            }

        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.d(
            TAG,
            "Accessibility service interrupted",
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()

        super.onDestroy()

        Log.d(
            TAG,
            "Accessibility service destroyed",
        )
    }
}
