package com.clouddroid.usagesafe.activities

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.fragments.*
import com.clouddroid.usagesafe.utils.ExtensionUtils.replaceWithTransaction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var selectedFragment: BaseFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkForUsagePermissions()
        initBottomNav()
    }

    private fun initBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener {
            val fragment =
                when (it.itemId) {
                    R.id.todaysStatsItem -> TodaysStatsFragment()
                    R.id.appLimitsItem -> AppLimitsFragment()
                    R.id.historyItem -> HistoryStatsFragment()
                    R.id.contactsListItem -> ContactsListFragment()
                    else -> TodaysStatsFragment()
                }

            selectedFragment = fragment

            supportFragmentManager.replaceWithTransaction(R.id.fragmentContainer, fragment)
            true
        }

        bottomNav.selectedItemId = R.id.todaysStatsItem
    }

    private fun checkForUsagePermissions() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
        )

        val granted = if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(AppOpsManager.OPSTR_GET_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }

        if (!granted) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}
