package com.example.debloater

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.debloater.IDebloaterService
import rikka.shizuku.SystemServiceHelper
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter

class DebloaterService : IDebloaterService.Stub() {

    // Only empty constructor is required. Shizuku provides privileged context automatically.
    private val context: Context
        get() = getApplicationContext()

    override fun uninstall(packageName: String) {
        executeShellCommand("pm uninstall --user 0 $packageName")
    }

    override fun disable(packageName: String) {
        // Disable without killing the app
        executeShellCommand("pm disable-user --user 0 $packageName")
    }

    private fun executeShellCommand(command: String) {
        try {
            val pfd = SystemServiceHelper.getSystemService("package")
                .openFileDescriptor("sh", "w") // Open shell for writing

            ParcelFileDescriptor.AutoCloseOutputStream(FileOutputStream(pfd.fileDescriptor)).use { os ->
                PrintWriter(os).use { writer ->
                    writer.println(command)
                    writer.println("exit")
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            Log.e("DebloaterService", "Failed to execute command: $command", e)
            throw e
        }
    }

    override fun destroy() {
        System.exit(0)
    }
}
