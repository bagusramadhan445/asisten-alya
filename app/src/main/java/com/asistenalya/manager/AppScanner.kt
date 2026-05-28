package com.asistenalya.manager

import android.content.Context
import android.content.Intent
import com.asistenalya.domain.model.AppInfo

class AppScanner(private val context: Context) {

    fun scanLauncherApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = context.packageManager.queryIntentActivities(mainIntent, 0)

        return resolveInfoList
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                AppInfo(
                    name = resolveInfo.loadLabel(context.packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(context.packageManager)
                )
            }
            .sortedBy { it.name.lowercase() }
            .distinctBy { it.packageName }
    }
}
