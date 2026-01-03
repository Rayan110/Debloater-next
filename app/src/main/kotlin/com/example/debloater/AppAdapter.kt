package com.example.debloater

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private var apps: List<PackageInfo>,
    private val packageManager: PackageManager,
    private val onActionClick: (String, String) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val pkg: TextView = view.findViewById(R.id.app_package)
        val uninstallButton: Button = view.findViewById(R.id.btn_uninstall)
        val disableButton: Button = view.findViewById(R.id.btn_disable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            0 -> R.layout.item_section_header
            else -> R.layout.item_app
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            0 -> { // Section header
                val title = if (position == 0) "Enabled Apps" else "Disabled Apps"
                holder.itemView.findViewById<TextView>(R.id.section_title)?.text = title
            }
            else -> {
                val adjustedPos = position - headerCountBefore(position)
                val app = apps[adjustedPos]
                val appInfo = app.applicationInfo!!

                holder.icon.setImageDrawable(appInfo.loadIcon(packageManager))
                holder.name.text = appInfo.loadLabel(packageManager)
                holder.pkg.text = app.packageName

                // System app → orange text
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                               (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                holder.name.setTextColor(if (isSystem) Color.parseColor("#FF9800") else Color.BLACK)

                // Disabled app → dimmed
                holder.itemView.alpha = if (appInfo.enabled) 1.0f else 0.5f

                holder.uninstallButton.setOnClickListener {
                    onActionClick(app.packageName, "uninstall")
                }
                holder.disableButton.setOnClickListener {
                    onActionClick(app.packageName, "disable")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val enabled = apps.count { it.applicationInfo!!.enabled }
        val disabled = apps.size - enabled
        var total = apps.size
        if (enabled > 0) total++
        if (disabled > 0) total++
        return total
    }

    override fun getItemViewType(position: Int): Int {
        val enabledCount = apps.count { it.applicationInfo!!.enabled }
        return when {
            enabledCount > 0 && position == 0 -> 0 // Enabled header
            position < enabledCount + (if (enabledCount > 0) 1 else 0) -> 1 // Enabled app
            disabledCount() > 0 && position == enabledCount + (if (enabledCount > 0) 1 else 0) -> 0 // Disabled header
            else -> 1 // Disabled app
        }
    }

    private fun disabledCount() = apps.count { !it.applicationInfo!!.enabled }

    private fun headerCountBefore(position: Int): Int {
        var count = 0
        val enabledCount = apps.count { it.applicationInfo!!.enabled }
        if (enabledCount > 0) count++
        if (position > enabledCount + count && disabledCount() > 0) count++
        return count
    }

    fun updateApps(newApps: List<PackageInfo>) {
        this.apps = newApps
        notifyDataSetChanged()
    }
}
