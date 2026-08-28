package com.allenrogers6.luzie.locker

import android.content.Context
import androidx.biometric.BiometricManager

fun canUseBiometrics(context: Context): Boolean {
    val manager =
        BiometricManager.from(context)

    return manager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK,
    ) == BiometricManager.BIOMETRIC_SUCCESS
}
