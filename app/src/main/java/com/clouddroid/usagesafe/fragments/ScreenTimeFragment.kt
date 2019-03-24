package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.utils.PackageInfoUtils
import com.clouddroid.usagesafe.utils.TextUtils
import com.clouddroid.usagesafe.viewmodels.HistoryStatsViewModel
import com.clouddroid.usagesafe.viewmodels.ScreenTimeViewModel
import com.github.mikephil.charting.components.XAxis
import kotlinx.android.synthetic.main.fragment_screen_time.*

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
            screenTimeViewModel.calculateWeeklyUsage(it)
        })

        screenTimeViewModel.barChartData.observe(this, Observer {
            barChart.data = it
            barChart.description.isEnabled = false
            barChart.setPinchZoom(false)
            barChart.isDoubleTapToZoomEnabled = false
            barChart.animateXY(500, 400)
            barChart.xAxis.setDrawGridLines(false)
            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChart.axisLeft.setDrawGridLines(false)
            barChart.axisLeft.setDrawAxisLine(false)
            barChart.axisLeft.setDrawLabels(false)
            barChart.axisRight.setDrawAxisLine(false)
            barChart.axisRight.setDrawGridLines(false)
            barChart.axisRight.setDrawLabels(false)
        })

        screenTimeViewModel.mostUsedApp.observe(this, Observer {
            Glide.with(context!!).load(PackageInfoUtils.getRawAppIcon(it.packageName, context)).into(mostUsedAppIcon)
            mostUsedTV.text = PackageInfoUtils.getAppName(it.packageName, context)
            mostUsedTimeTV.text = TextUtils.getTotalScreenTimeText(it.totalTimeInForeground, context)
        })

        screenTimeViewModel.totalTimeSum.observe(this, Observer {
            val avgPerDay = it / 7
            val avgPerHour = it / 7 / 24
            avgPerDayNumber.text = TextUtils.getTotalScreenTimeText(avgPerDay, context)
            avgPerHourNumber.text = TextUtils.getTotalScreenTimeText(avgPerHour, context)

            weeklyUsageSummaryTV.text = "${TextUtils.getTotalScreenTimeText(it, context)} of usage this week"
        })
    }
}