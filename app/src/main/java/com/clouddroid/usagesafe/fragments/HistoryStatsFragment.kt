package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.activities.MainActivity
import com.clouddroid.usagesafe.adapters.ViewPagerAdapter
import com.clouddroid.usagesafe.viewmodels.HistoryStatsViewModel
import kotlinx.android.synthetic.main.fragment_history_stats.*

class HistoryStatsFragment : BaseFragment() {

    private val screenTimeFragment = ScreenTimeFragment()
    private val unlocksFragment = UnlocksFragment()
    private val appLaunchesFragment = AppLaunchesFragment()

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
    }

    fun scrollToTop() {
        when (tabLayout.selectedTabPosition) {
            0 -> screenTimeFragment.scrollToTop()
            1 -> unlocksFragment.scrollToTop()
            2 -> appLaunchesFragment.scrollToTop()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        viewModel.updateCurrentWeek()
    }

    private fun setUpViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPager.offscreenPageLimit = 2

        viewPagerAdapter.addFragment(screenTimeFragment, "Screen time")
        viewPagerAdapter.addFragment(unlocksFragment, "Unlocks")
        viewPagerAdapter.addFragment(appLaunchesFragment, "App launches")

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