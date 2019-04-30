package com.clouddroid.usagesafe.ui.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.applimits.AppLimitsFragment
import com.clouddroid.usagesafe.ui.base.BaseActivity
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.contacts.ContactsListFragment
import com.clouddroid.usagesafe.ui.historystats.HistoryStatsFragment
import com.clouddroid.usagesafe.ui.todaystats.TodaysStatsFragment
import com.clouddroid.usagesafe.util.ExtensionUtils.addAndCommit
import com.clouddroid.usagesafe.util.ExtensionUtils.doesNotContain
import com.clouddroid.usagesafe.util.ExtensionUtils.showAndHideOthers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private val todaysStatsFragment = TodaysStatsFragment()
    private val appLimitsFragment = AppLimitsFragment()
    private val historyStatsFragment = HistoryStatsFragment()
    private val contactsListFragment = ContactsListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
        initFragmentsBackStack()
        initBottomNav()
        checkForUsagePermissions()
    }

    override fun onBackPressed() {

        //delegating back button click event to fragments
        var clickHandled = false
        supportFragmentManager.fragments.forEach {
            when (it) {
                is BaseFragment -> clickHandled = it.handleBackButtonPress()
            }
        }

        if (!todaysStatsFragment.isVisible && !clickHandled) {
            navigateTo(FragmentDestination.TODAYS_STATS)
            bottomNav.selectedItemId = R.id.todaysStatsFragment
        } else {
            if (!clickHandled) {
                super.onBackPressed()
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainActivityViewModel::class.java]
        viewModel.init()
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

        bottomNav.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.todaysStatsFragment -> todaysStatsFragment.scrollToTop()
                R.id.appLimitsFragment -> appLimitsFragment.scrollToTop()
                R.id.historyStatsFragment -> historyStatsFragment.scrollToTop()
                R.id.contactsListFragment -> contactsListFragment.scrollToTop()
                else -> {
                }
            }
        }
    }

    private fun navigateTo(destination: FragmentDestination) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.slide_up, 0, 0,
                R.animator.slide_down
            )

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

            FragmentDestination.SCREEN_TIME -> transaction.showAndHideOthers(
                historyStatsFragment,
                supportFragmentManager.fragments
            )

            FragmentDestination.UNLOCKS -> transaction.showAndHideOthers(
                historyStatsFragment,
                supportFragmentManager.fragments
            )

            FragmentDestination.APP_LAUNCHES -> transaction.showAndHideOthers(
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

    //function to be called from fragments
    fun switchToFragment(destination: FragmentDestination) {
        navigateTo(destination)
        when (destination) {
            FragmentDestination.APP_LAUNCHES -> {
                bottomNav.selectedItemId = R.id.historyStatsFragment
                historyStatsFragment.slideTo(destination)
            }
            FragmentDestination.UNLOCKS -> {
                bottomNav.selectedItemId = R.id.historyStatsFragment
                historyStatsFragment.slideTo(destination)
            }
            else -> {
            }
        }
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
        CONTACTS_LIST,
        SCREEN_TIME,
        UNLOCKS,
        APP_LAUNCHES
    }
}
