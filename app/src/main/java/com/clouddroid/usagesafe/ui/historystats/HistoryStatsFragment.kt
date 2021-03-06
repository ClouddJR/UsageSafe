package com.clouddroid.usagesafe.ui.historystats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.historystats.applaunches.AppLaunchesFragment
import com.clouddroid.usagesafe.ui.historystats.screen.ScreenTimeFragment
import com.clouddroid.usagesafe.ui.historystats.unlocks.UnlocksFragment
import com.clouddroid.usagesafe.ui.main.MainActivity
import com.clouddroid.usagesafe.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_history_stats.*

class HistoryStatsFragment : BaseFragment() {

    companion object {
        const val TAG = "HistoryStatsFragment"
    }

    private lateinit var viewModel: HistoryStatsViewModel

    override fun getLayoutId() = R.layout.fragment_history_stats

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()
        observeData()
        setOnWeekChangeListener()
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.init()
    }

    fun scrollToTop() {
        val fragment = childFragmentManager.findFragmentByTag(
            "android:switcher:"
                    + viewPager.id + ":"
                    + tabLayout.selectedTabPosition
        )
        when (fragment) {
            is ScreenTimeFragment -> fragment.scrollToTop()
            is UnlocksFragment -> fragment.scrollToTop()
            is AppLaunchesFragment -> fragment.scrollToTop()
        }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
    }

    private fun setUpViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(requireContext(), childFragmentManager)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)
    }

    private fun observeData() {
        viewModel.currentWeekText.observe(this, Observer {
            dateTV.text = it
        })

        viewModel.shouldLeftArrowBeHidden.observe(this, Observer {
            when (it) {
                true -> arrowLeft.visibility = View.INVISIBLE
                false -> arrowLeft.visibility = View.VISIBLE
            }
        })

        viewModel.shouldRightArrowBeHidden.observe(this, Observer {
            when (it) {
                true -> arrowRight.visibility = View.INVISIBLE
                false -> arrowRight.visibility = View.VISIBLE
            }
        })
    }

    private fun setOnWeekChangeListener() {
        arrowRight.setOnClickListener {
            viewModel.rightArrowClicked()
        }

        arrowLeft.setOnClickListener {
            viewModel.leftArrowClicked()
        }
    }

    private fun setOnClickListeners() {
        settingsIcon.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    fun slideTo(destination: MainActivity.FragmentDestination) {
        val pageIndex = when (destination) {
            MainActivity.FragmentDestination.UNLOCKS -> 1
            MainActivity.FragmentDestination.APP_LAUNCHES -> 2
            else -> 0
        }

        tabLayout.setScrollPosition(pageIndex, 0f, true)
        viewPager.currentItem = pageIndex

    }
}