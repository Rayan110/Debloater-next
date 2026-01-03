package com.example.debloater

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ShizukuManager – this will handle binding automatically
        ShizukuManager.init(this)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val apps = getInstalledApps()
        adapter = AppAdapter(apps) { packageName, action ->
            when (action) {
                "uninstall" -> ShizukuManager.uninstall(packageName)
                "disable" -> ShizukuManager.disable(packageName)
            }
        }
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        ShizukuManager.cleanup()
        super.onDestroy()
    }

    private fun getInstalledApps(): List<PackageInfo> {
    val pm = packageManager
    // Use 0 or MATCH_ALL – both work the same here; 0 is simplest and always returns all with the permission
    val packages = pm.getInstalledPackages(0)

    return packages
        .filter { it.applicationInfo != null }  // Safety filter (rare null cases)
        .sortedBy { 
            pm.getApplicationLabel(it.applicationInfo!!).toString().lowercase()
        }
}
