package com.example.debloater

import android.content.Context
import android.content.pm.PackageManager
import com.example.debloater.IDebloaterService

class DebloaterService : IDebloaterService.Stub() {

    private lateinit var context: Context

    constructor() : super() // Required empty constructor

    constructor(context: Context) : super() {
        this.context = context
    }

    override fun uninstall(packageName: String) {
        // Equivalent to "pm uninstall --user 0" (safe for system apps via Shizuku ADB)
        context.packageManager.packageInstaller.uninstall(packageName, null)
    }

    override fun disable(packageName: String) {
        context.packageManager.setApplicationEnabledSetting(
            packageName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
            0
        )
    }

    override fun destroy() {
        System.exit(0)
    }
}
