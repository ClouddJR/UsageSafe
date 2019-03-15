package com.clouddroid.usagesafe.activities

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.fragments.AppLimitsFragment
import com.clouddroid.usagesafe.fragments.ContactsListFragment
import com.clouddroid.usagesafe.fragments.HistoryStatsFragment
import com.clouddroid.usagesafe.fragments.TodaysStatsFragment
import com.clouddroid.usagesafe.utils.ExtensionUtils.addAndCommit
import com.clouddroid.usagesafe.utils.ExtensionUtils.doesNotContain
import com.clouddroid.usagesafe.utils.ExtensionUtils.showAndHideOthers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val todaysStatsFragment = TodaysStatsFragment()
    private val appLimitsFragment = AppLimitsFragment()
    private val historyStatsFragment = HistoryStatsFragment()
    private val contactsListFragment = ContactsListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkForUsagePermissions()
        initBottomNav()
        initFragmentsBackStack()

    }

    private fun initFragmentsBackStack() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentPlaceHolder, todaysStatsFragment)
            .add(R.id.fragmentPlaceHolder, appLimitsFragment)
            .add(R.id.fragmentPlaceHolder, historyStatsFragment)
            .add(R.id.fragmentPlaceHolder, contactsListFragment)
            .hide(appLimitsFragment)
            .hide(historyStatsFragment)
            .hide(contactsListFragment)
            .commit()
        bottomNav.menu.findItem(R.id.todaysStatsFragment).isChecked = true
    }

    private fun initBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.todaysStatsFragment -> navigateTo(FragmentDestination.TODAYS_STATS)
                R.id.appLimitsFragment -> navigateTo(FragmentDestination.APP_LIMITS)
                R.id.historyStatsFragment -> navigateTo(FragmentDestination.HISTORY)
                R.id.contactsListFragment -> navigateTo(FragmentDestination.CONTACTS_LIST)
                else -> navigateTo(FragmentDestination.TODAYS_STATS)
            }
            true
        }
    }

    private fun navigateTo(destination: FragmentDestination) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()

        //addFragmentIfNotInBackStack(destination)

        when (destination) {
            FragmentDestination.TODAYS_STATS -> transaction.showAndHideOthers(
                todaysStatsFragment,
                supportFragmentManager.fragments
            )
            FragmentDestination.APP_LIMITS -> transaction.showAndHideOthers(
                appLimitsFragment,
                supportFragmentManager.fragments
            )
            FragmentDestination.HISTORY -> transaction.showAndHideOthers(
                historyStatsFragment,
                supportFragmentManager.fragments
            )
            FragmentDestination.CONTACTS_LIST -> transaction.showAndHideOthers(
                contactsListFragment,
                supportFragmentManager.fragments
            )
        }

        transaction.commit()
    }

    private fun addFragmentIfNotInBackStack(destination: FragmentDestination) {
        when (destination) {
            FragmentDestination.APP_LIMITS -> {
                if (supportFragmentManager.doesNotContain(appLimitsFragment)) {
                    supportFragmentManager.addAndCommit(appLimitsFragment)
                }
            }
            FragmentDestination.HISTORY -> {
                if (supportFragmentManager.doesNotContain(historyStatsFragment)) {
                    supportFragmentManager.addAndCommit(historyStatsFragment)
                }
            }
            FragmentDestination.CONTACTS_LIST -> {
                if (supportFragmentManager.doesNotContain(contactsListFragment)) {
                    supportFragmentManager.addAndCommit(contactsListFragment)
                }
            }
            else -> {
            }
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

    enum class FragmentDestination {
        TODAYS_STATS,
        APP_LIMITS,
        HISTORY,
        CONTACTS_LIST
    }
}
