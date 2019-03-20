package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.viewmodels.HistoryStatsViewModel
import com.clouddroid.usagesafe.viewmodels.ScreenTimeViewModel

class ScreenTimeFragment : BaseFragment() {

    private lateinit var historyStatsViewModel: HistoryStatsViewModel
    private lateinit var screenTimeViewModel: ScreenTimeViewModel

    override fun getLayoutId() = R.layout.fragment_screen_time

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun initViewModels() {
        historyStatsViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        screenTimeViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[ScreenTimeViewModel::class.java]
    }

    private fun observeData() {
        historyStatsViewModel.weeklyData.observe(this, Observer {

        })
    }
}