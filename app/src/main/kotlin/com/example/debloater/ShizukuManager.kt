package com.example.debloater

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import rikka.shizuku.Shizuku

object ShizukuManager {

    private const val REQUEST_CODE = 1000
    private var debloaterService: IDebloaterService? = null
    private var isBound = false

    private lateinit var context: Context

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE) {
            if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Shizuku permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Shizuku permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            debloaterService = IDebloaterService.Stub.asInterface(service)
            isBound = true
            Toast.makeText(context, "Shizuku service connected", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            debloaterService = null
            isBound = false
        }
    }

    fun init(context: Context) {
        this.context = context.applicationContext
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
    }

    fun cleanup() {
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        if (isBound) {
            Shizuku.unbindUserService(serviceArgs(), serviceConnection, true)
            isBound = false
        }
    }

    private fun serviceArgs() = Shizuku.UserServiceArgs(
        ComponentName(context.packageName, DebloaterService::class.java.name)
    )
        .daemon(false)
        .debuggable(false)
        .version(1)
        .tag("debloater")

    fun checkAndRequestPermission(): Boolean {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(context, "Shizuku is not running. Start it first.", Toast.LENGTH_LONG).show()
            return false
        }

        if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return true
        }

        Shizuku.requestPermission(REQUEST_CODE)
        return false
    }

    fun bindService() {
        if (!checkAndRequestPermission()) return

        Shizuku.bindUserService(serviceArgs(), serviceConnection)
    }

    fun uninstall(packageName: String) {
        if (!isBound || debloaterService == null) {
            Toast.makeText(context, "Shizuku not connected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            debloaterService?.uninstall(packageName)
            Toast.makeText(context, "Uninstall sent for $packageName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Uninstall failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun disable(packageName: String) {
        if (!isBound || debloaterService == null) {
            Toast.makeText(context, "Shizuku not connected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            debloaterService?.disable(packageName)
            Toast.makeText(context, "Disabled $packageName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Disable failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
