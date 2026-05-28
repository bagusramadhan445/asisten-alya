package com.asistenalya.manager

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.asistenalya.service.AlyaAccessibilityService
import com.asistenalya.service.AlyaDeviceAdminReceiver

class AlyaPowerManager(private val context: Context) {

    fun lockScreen(): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, AlyaDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openPowerDialog(): Boolean {
        return try {
            val service = AlyaAccessibilityService.instance
            service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isDeviceAdminActive(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AlyaDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(adminComponent)
    }
}
