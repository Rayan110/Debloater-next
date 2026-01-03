package com.example.debloater

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private lateinit var searchView: SearchView
    private lateinit var refreshButton: Button

    private val pm: PackageManager by lazy { packageManager }
    private var allApps: List<PackageInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ShizukuManager.init(this)

        recyclerView = findViewById(R.id.recycler_view)
        searchView = findViewById(R.id.search_view)
        refreshButton = findViewById(R.id.btn_refresh)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadApps()

        adapter = AppAdapter(allApps, pm) { packageName, action ->
            when (action) {
                "uninstall" -> {
                    ShizukuManager.uninstall(packageName)
                    refreshList()
                }
                "disable" -> {
                    ShizukuManager.disable(packageName)
                    refreshList()
                }
            }
        }
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText.orEmpty())
                return true
            }
        })

        refreshButton.setOnClickListener {
            loadApps()
        }
    }

    private fun loadApps() {
        try {
            allApps = pm.getInstalledPackages(0)
                .filter { pkg ->
                    pkg.applicationInfo != null &&  // Skip null
                    try {
                        pkg.applicationInfo!!.loadLabel(pm).isNotEmpty()  // Skip if label fails
                    } catch (e: Exception) {
                        false
                    }
                }
                .sortedBy { 
                    try {
                        pm.getApplicationLabel(it.applicationInfo!!).toString().lowercase()
                    } catch (e: Exception) {
                        it.packageName.lowercase()  // Fallback to package name
                    }
                }

            adapter.updateApps(allApps)
            filterApps(searchView.query?.toString().orEmpty())
        } catch (e: Exception) {
            // Rare full failure – show empty list safely
            allApps = emptyList()
            adapter.updateApps(allApps)
        }
    }

    private fun filterApps(query: String) {
        val lowerQuery = query.lowercase()
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { pkg ->
                val appInfo = pkg.applicationInfo!!
                val label = try { pm.getApplicationLabel(appInfo).toString().lowercase() } catch (e: Exception) { "" }
                val pkgName = pkg.packageName.lowercase()
                label.contains(lowerQuery) || pkgName.contains(lowerQuery)
            }
        }
        adapter.updateApps(filtered)
    }

    private fun refreshList() {
        Handler(Looper.getMainLooper()).postDelayed({
            loadApps()
        }, 1200)  // Slightly longer delay for system changes
    }

    override fun onDestroy() {
        ShizukuManager.cleanup()
        super.onDestroy()
    }
}
