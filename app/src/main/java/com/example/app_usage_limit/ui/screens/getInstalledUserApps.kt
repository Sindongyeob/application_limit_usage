package com.example.app_usage_limit.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun getInstalledUserApps(packageManager: PackageManager): List<ApplicationInfo> {
    return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .sortedBy { packageManager.getApplicationLabel(it).toString() }
}
