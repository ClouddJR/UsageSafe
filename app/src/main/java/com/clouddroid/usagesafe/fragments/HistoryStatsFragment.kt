package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.adapters.ViewPagerAdapter
import com.clouddroid.usagesafe.viewmodels.HistoryStatsViewModel
import kotlinx.android.synthetic.main.fragment_history_stats.*

class HistoryStatsFragment : BaseFragment() {

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

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        viewModel.setCurrentWeek(viewModel.getTodayCalendar())
    }

    private fun setUpViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)

        viewPagerAdapter.addFragment(ScreenTimeFragment(), "Screen time")
        viewPagerAdapter.addFragment(UnlocksFragment(), "Unlocks")
        viewPagerAdapter.addFragment(AppsUsageFragment(), "Apps usage")

        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)
    }

    private fun observeData() {
        viewModel.currentWeekText.observe(this, Observer {
            dateTV.text = it
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
}