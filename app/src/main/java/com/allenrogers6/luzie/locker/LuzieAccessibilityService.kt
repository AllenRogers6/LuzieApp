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
import kotlinx.coroutines.delay
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

        val packageName = event.packageName?.toString() ?: return

        if (packageName == SETTINGS_PACKAGE) {
            serviceScope.launch {
                handleDeviceAdminConfirmation()
            }
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

    private fun collectAccessibilityText(node: AccessibilityNodeInfo): List<String> {
        val result = mutableListOf<String>()

        node.text?.toString()?.let {
            if (it.isNotBlank()) result += it
        }

        node.contentDescription?.toString()?.let {
            if (it.isNotBlank()) result += it
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                result += collectAccessibilityText(child)
                child.recycle()
            }
        }

        return result
    }

    private fun isLuzieDisableConfirmationDialog(): Boolean {
        val root = rootInActiveWindow ?: return false

        val text = collectAccessibilityText(root)

        val hasLuzieWarning =
            text.any {
                it.contains("Luzie anti-uninstall", ignoreCase = true)
            }

        val hasConfirmationButton =
            text.any {
                it.equals("OK", ignoreCase = true) ||
                    it.equals("Deactivate", ignoreCase = true)
            }

        return hasLuzieWarning && hasConfirmationButton
    }

    private suspend fun handleDeviceAdminConfirmation() {
        if (!isLuzieDeviceAdmin(applicationContext)) return

        if (!preferences.adminDisableAttempt.first()) return

        if (!isLuzieDisableConfirmationDialog()) return

        val authorized =
            preferences
                .adminDeactivationAuthorized
                .first()

        if (authorized) {
            Log.d(
                TAG,
                "Deactivation attempt already authorized",
            )
            return
        }

        Log.d(
            TAG,
            "Luzie admin deactivation confirmation detected",
        )

        launchAntiUninstallLock()
    }

    private fun handleDeviceAdminSettings() {
        if (antiUninstallScreenDetected) {
            return
        }

        serviceScope.launch {
            val antiUninstallEnabled =
                preferences
                    .isAntiUninstall
                    .first()

            if (!antiUninstallEnabled) {
                return@launch
            }

        /*
         * Only intervene while Luzie is actually an
         * active Device Administrator.
         */
            if (
                !isLuzieDeviceAdmin(
                    applicationContext,
                )
            ) {
                return@launch
            }

        /*
         * IMPORTANT:
         *
         * We are in the Android Settings app, but that
         * does NOT mean we're on the Device Admin page.
         */
            if (!isLuzieDeactivationScreen()) {
                return@launch
            }

            antiUninstallScreenDetected = true

            Log.d(
                TAG,
                "Luzie Device Admin deactivation screen detected",
            )

        /*
         * Leave the deactivation screen before launching
         * our authentication activity.
         */
            performGlobalAction(
                GLOBAL_ACTION_BACK,
            )

            delay(150)

            launchAntiUninstallLock()
        }
    }

    private fun isLuzieDeactivationScreen(): Boolean {
        val root =
            rootInActiveWindow
                ?: return false

    /*
     * First verify that the Settings hierarchy
     * actually contains Luzie.
     */
        val luzieNodes =
            root.findAccessibilityNodeInfosByText(
                "Luzie",
            )

        if (luzieNodes.isEmpty()) {
            return false
        }

    /*
     * Then look for deactivation-specific text.
     *
     * Do NOT treat generic "Battery", "App battery usage",
     * "Allow background usage", etc. as a match.
     */
        val deactivateTexts =
            listOf(
                "Deactivate",
                "Deactivate this device admin app",
                "Deactivate admin",
                "Turn off device administrator",
            )

        return deactivateTexts.any { text ->
            root
                .findAccessibilityNodeInfosByText(
                    text,
                ).isNotEmpty()
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
        val intent =
            Intent(
                applicationContext,
                AppLock::class.java,
            ).apply {
                putExtra(
                    AppLock.EXTRA_ANTI_UNINSTALL,
                    true,
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                )
            }

        applicationContext.startActivity(intent)
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
