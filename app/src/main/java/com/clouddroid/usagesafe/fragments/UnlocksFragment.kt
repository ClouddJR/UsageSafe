package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.viewmodels.HistoryStatsViewModel
import com.clouddroid.usagesafe.viewmodels.UnlocksViewModel
import com.github.mikephil.charting.components.XAxis
import kotlinx.android.synthetic.main.fragment_unlocks.*

class UnlocksFragment : BaseFragment() {

    private lateinit var historyStatsViewModel: HistoryStatsViewModel
    private lateinit var unlocksViewModel: UnlocksViewModel

    override fun getLayoutId() = R.layout.fragment_unlocks

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModels()
    }

    private fun initViewModels() {
        historyStatsViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        unlocksViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[UnlocksViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        historyStatsViewModel.weeklyData.observe(this, Observer {
            unlocksViewModel.calculateWeeklyUnlocks(it)
        })

        unlocksViewModel.barChartData.observe(this, Observer {
            barChart.data = it
            barChart.description.isEnabled = false
            barChart.animateXY(500, 400)
            barChart.setPinchZoom(false)
            barChart.isDoubleTapToZoomEnabled = false
            barChart.xAxis.setDrawGridLines(false)
            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChart.axisLeft.setDrawGridLines(false)
            barChart.axisLeft.setDrawAxisLine(false)
            barChart.axisLeft.setDrawLabels(false)
            barChart.axisRight.setDrawAxisLine(false)
            barChart.axisRight.setDrawGridLines(false)
            barChart.axisRight.setDrawLabels(false)
        })

        unlocksViewModel.totalUnlocks.observe(this, Observer {
            val avgPerDay = it / 7
            val avgPerHour = it / 7 / 24
            avgPerDayNumber.text = avgPerDay.toString()
            avgPerHourNumber.text = avgPerHour.toString()

            numberOfUnlocksTV.text = "$it screen unlocks this week"
        })
    }
}