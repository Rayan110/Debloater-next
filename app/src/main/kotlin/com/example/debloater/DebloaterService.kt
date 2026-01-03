package com.example.debloater

import android.content.Context
import android.content.pm.IPackageManager
import android.os.RemoteException
import com.example.debloater.IDebloaterService
import rikka.shizuku.SystemServiceHelper

class DebloaterService : IDebloaterService.Stub() {

    // Support both new (v13+) and old Shizuku versions
    constructor() : super()

    constructor(context: Context) : super() {
        // Context is provided by Shizuku v13+, but we don't need it here
    }

    private val packageManager: IPackageManager by lazy {
        IPackageManager.Stub.asInterface(SystemServiceHelper.getSystemService("package"))
    }

    override fun uninstall(packageName: String) {
        try {
            // pm uninstall --user 0 <package>
            packageManager.deletePackageAsUser(packageName, 0, 0)
            // Alternative if above fails on some ROMs: packageManager.getPackageInstaller().uninstall(packageName, null)
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        }
    }

    override fun disable(packageName: String) {
        try {
            // pm disable-user --user 0 <package>
            packageManager.setApplicationEnabledSetting(
                packageName,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                0,
                0,  // user 0
                null
            )
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        }
    }

    override fun destroy() {
        System.exit(0)
    }
}
