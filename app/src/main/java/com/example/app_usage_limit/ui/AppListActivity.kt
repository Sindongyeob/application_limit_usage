package com.example.app_usage_limit.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_usage_limit.R

class AppListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: AppListAdapter
    private lateinit var allApps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)  // ✅ 반드시 activity_app_list.xml 사용

        recyclerView = findViewById(R.id.app_list_view)
        searchEditText = findViewById(R.id.search_edit_text)

        recyclerView.layoutManager = LinearLayoutManager(this)

        allApps = getInstalledApps(this)

        adapter = AppListAdapter(allApps.toMutableList()) { app ->
            val intent = Intent(this, AppTimeSettingActivity::class.java)
            intent.putExtra("PACKAGE_NAME", app.packageName)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterApps(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterApps(query: String) {
        val filtered = allApps.filter {
            it.appName.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }

    private fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedApps = pm.queryIntentActivities(intent, 0)
        val myPackageName = context.packageName

        for (resolveInfo in resolvedApps) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val packageName = appInfo.packageName

            if (packageName != myPackageName) {
                val appName = pm.getApplicationLabel(appInfo).toString()
                val appIcon = try {
                    pm.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!
                }
                apps.add(AppInfo(appName, packageName, appIcon))
            }
        }

        return apps.sortedBy { it.appName.lowercase() }
    }
}
