package com.example.app_usage_limit.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_usage_limit.R

class AppListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        recyclerView = findViewById(R.id.app_list_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val appList = getInstalledApps(this)
        val adapter = AppListAdapter(appList) { app ->
            val intent = Intent(this, AppTimeSettingActivity::class.java)
            intent.putExtra("PACKAGE_NAME", app.packageName)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedApps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val myPackageName = context.packageName

        for (resolveInfo in resolvedApps) {
            val pkgInfo = resolveInfo.activityInfo.applicationInfo
            if (pkgInfo.packageName != myPackageName) {
                val appName = pm.getApplicationLabel(pkgInfo).toString()
                val appIcon = pm.getApplicationIcon(pkgInfo)
                apps.add(AppInfo(appName, pkgInfo.packageName, appIcon))
            }
        }

        return apps.sortedBy { it.appName.lowercase() }
    }
}
