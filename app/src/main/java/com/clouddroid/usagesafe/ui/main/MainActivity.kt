package com.clouddroid.usagesafe.ui.main

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.applimits.AppLimitsFragment
import com.clouddroid.usagesafe.ui.base.BaseActivity
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.historystats.HistoryStatsFragment
import com.clouddroid.usagesafe.ui.todaystats.TodaysStatsFragment
import com.clouddroid.usagesafe.ui.welcome.PermissionActivity
import com.clouddroid.usagesafe.util.ExtensionUtils.addAndCommit
import com.clouddroid.usagesafe.util.ExtensionUtils.doesNotContain
import com.clouddroid.usagesafe.util.ExtensionUtils.showAndHideOthers
import com.clouddroid.usagesafe.util.PreferencesUtils.defaultPrefs
import com.clouddroid.usagesafe.util.purchase.IabHelper
import com.clouddroid.usagesafe.util.purchase.PurchasesUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var todaysStatsFragment: TodaysStatsFragment
    private lateinit var appLimitsFragment: AppLimitsFragment
    private lateinit var historyStatsFragment: HistoryStatsFragment

    private lateinit var helper: IabHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        redirectIfPermissionNotGranted()
        initViewModel()
        restoreFragmentInstances()
        if (savedInstanceState == null) {
            initFragmentsBackStack()
        }
        initBottomNav()
        checkForPurchases()
    }

    override fun onResume() {
        super.onResume()
        viewModel.init()
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
    }

    private fun restoreFragmentInstances() {
        todaysStatsFragment = (supportFragmentManager.findFragmentByTag(TodaysStatsFragment.TAG)
            ?: TodaysStatsFragment()) as TodaysStatsFragment
        historyStatsFragment = (supportFragmentManager.findFragmentByTag(HistoryStatsFragment.TAG)
            ?: HistoryStatsFragment()) as HistoryStatsFragment
        appLimitsFragment = (supportFragmentManager.findFragmentByTag(AppLimitsFragment.TAG)
            ?: AppLimitsFragment()) as AppLimitsFragment
    }

    private fun initFragmentsBackStack() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentPlaceHolder, todaysStatsFragment, TodaysStatsFragment.TAG)
            //.add(R.id.fragmentPlaceHolder, appLimitsFragment)
            //.add(R.id.fragmentPlaceHolder, historyStatsFragment)
            //.hide(appLimitsFragment)
            //.hide(historyStatsFragment)
            .commit()
        bottomNav.menu.findItem(R.id.todaysStatsFragment).isChecked = true
    }

    private fun initBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.todaysStatsFragment -> navigateTo(FragmentDestination.TODAYS_STATS)
                R.id.appLimitsFragment -> navigateTo(FragmentDestination.APP_LIMITS)
                R.id.historyStatsFragment -> navigateTo(FragmentDestination.HISTORY)
                else -> navigateTo(FragmentDestination.TODAYS_STATS)
            }
            true
        }

        bottomNav.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.todaysStatsFragment -> todaysStatsFragment.scrollToTop()
                R.id.appLimitsFragment -> appLimitsFragment.scrollToTop()
                R.id.historyStatsFragment -> historyStatsFragment.scrollToTop()
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

        addFragmentIfNotInBackStack(destination)

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
                    supportFragmentManager.addAndCommit(appLimitsFragment, AppLimitsFragment.TAG)
                }
            }
            FragmentDestination.HISTORY -> {
                if (supportFragmentManager.doesNotContain(historyStatsFragment)) {
                    supportFragmentManager.addAndCommit(
                        historyStatsFragment,
                        HistoryStatsFragment.TAG
                    )
                }
            }
            else -> {
            }
        }
    }

    private fun redirectIfPermissionNotGranted() {
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
            startActivity<PermissionActivity>()
            finish()
        }
    }

    private fun checkForPurchases() {
        helper = IabHelper(this, PurchasesUtils.base64EncodedPublicKey)
        PurchasesUtils.sharedPreferences = defaultPrefs(this)
        helper.startSetup { result ->
            if (!result.isSuccess) {
                // Problem
            } else {
                try {
                    helper.queryInventoryAsync(PurchasesUtils.GotInventoryListener)
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
        }
    }

    enum class FragmentDestination {
        TODAYS_STATS,
        APP_LIMITS,
        HISTORY,
        SCREEN_TIME,
        UNLOCKS,
        APP_LAUNCHES
    }
}
