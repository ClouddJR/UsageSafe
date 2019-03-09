package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.viewmodels.TodaysStatsViewModel


class TodaysStatsFragment : BaseFragment() {

    private lateinit var viewModel: TodaysStatsViewModel

    override fun getLayoutId() = com.clouddroid.usagesafe.R.layout.fragment_todays_stats

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getTodaysStats()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[TodaysStatsViewModel::class.java]
    }
}