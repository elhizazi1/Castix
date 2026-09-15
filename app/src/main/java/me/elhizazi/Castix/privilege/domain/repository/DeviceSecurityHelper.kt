package me.elhizazi.Castix.privilege.domain.repository

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build

data class DeviceLockInfo(
    val isDeviceSecure: Boolean,
    val isCurrentlyLocked: Boolean
)

/**
 * Robust device lock security detector combining Android KeyguardManager,
 * DevicePolicyManager, and Biometric capabilities.
 */
object DeviceSecurityHelper {

    fun getDeviceLockInfo(context: Context): DeviceLockInfo {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isCurrentlyLocked = keyguard?.isDeviceLocked == true || keyguard?.isKeyguardLocked == true

        var isSecure = false

        if (keyguard != null) {
            // 1. Modern Android isDeviceSecure (API 23+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (keyguard.isDeviceSecure) {
                    isSecure = true
                }
            }

            // 2. Standard isKeyguardSecure
            if (!isSecure && keyguard.isKeyguardSecure) {
                isSecure = true
            }

            // 3. Confirm Device Credential Intent check
            if (!isSecure && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val intent = keyguard.createConfirmDeviceCredentialIntent(null, null)
                    if (intent != null) {
                        isSecure = true
                    }
                } catch (_: Exception) {}
            }
        }

        // 4. Biometrics & Device Credential Framework (Android 10+ / API 29+)
        if (!isSecure && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val bm = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
                val canAuth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bm?.canAuthenticate(
                        android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                } else {
                    @Suppress("DEPRECATION")
                    bm?.canAuthenticate()
                }
                if (canAuth == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS) {
                    isSecure = true
                }
            } catch (_: Exception) {}
        }

        // 5. DevicePolicyManager check as fallback
        if (!isSecure) {
            try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                if (dpm != null && dpm.getPasswordQuality(null) != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) {
                    isSecure = true
                }
            } catch (_: Exception) {}
        }

        return DeviceLockInfo(
            isDeviceSecure = isSecure,
            isCurrentlyLocked = isCurrentlyLocked
        )
    }
}
