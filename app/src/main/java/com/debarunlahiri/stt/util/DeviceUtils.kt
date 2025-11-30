package com.debarunlahiri.stt.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object DeviceUtils {
    fun isWearOs(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    }
}
