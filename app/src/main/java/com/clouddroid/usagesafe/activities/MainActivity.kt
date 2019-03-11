package com.clouddroid.usagesafe.activities

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.clouddroid.usagesafe.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkForUsagePermissions()
        initNavController()
        initBottomNav()
    }

    private fun initNavController() {
        navController = Navigation.findNavController(this, R.id.fragmentPlaceHolder)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.todaysStatsFragment -> bottomNav.menu.findItem(R.id.todaysStatsItem).isChecked = true
                R.id.appLimitsFragment -> bottomNav.menu.findItem(R.id.appLimitsItem).isChecked = true
                R.id.historyStatsFragment -> bottomNav.menu.findItem(R.id.historyItem).isChecked = true
                R.id.contactsListFragment -> bottomNav.menu.findItem(R.id.contactsListItem).isChecked = true
            }
        }
    }

    private fun initBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.todaysStatsItem -> navigateTo(R.id.todaysStatsFragment)
                R.id.appLimitsItem -> navigateTo(R.id.appLimitsFragment)
                R.id.historyItem -> navigateTo(R.id.historyStatsFragment)
                R.id.contactsListItem -> navigateTo(R.id.contactsListFragment)
                else -> navigateTo(R.id.todaysStatsFragment)
            }
            true
        }
    }

    private fun navigateTo(resourceId: Int) {

        if (!navController.popBackStack(resourceId, false) && navController.currentDestination?.id != resourceId) {
            navController.navigate(resourceId)
        }

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
