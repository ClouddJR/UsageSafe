package com.clouddroid.usagesafe.fragments

import android.os.Bundle
import android.view.View
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.adapters.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_history_stats.*

class HistoryStatsFragment : BaseFragment() {

    override fun getLayoutId() = R.layout.fragment_history_stats

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)

        viewPagerAdapter.addFragment(ScreenTimeFragment(), "Screen time")
        viewPagerAdapter.addFragment(UnlocksFragment(), "Unlocks")
        viewPagerAdapter.addFragment(AppsUsageFragment(), "Apps usage")

        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)
    }
}