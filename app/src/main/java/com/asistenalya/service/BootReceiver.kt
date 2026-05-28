package com.asistenalya.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.asistenalya.utils.SecurePrefs

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (SecurePrefs.isOverlayEnabled(context) && Settings.canDrawOverlays(context)) {
                OverlayService.start(context)
            }
        }
    }
}
