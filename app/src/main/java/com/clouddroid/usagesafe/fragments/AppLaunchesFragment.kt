package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.utils.PackageInfoUtils
import com.clouddroid.usagesafe.viewmodels.AppLaunchesViewModel
import com.clouddroid.usagesafe.viewmodels.HistoryStatsViewModel
import com.github.mikephil.charting.components.XAxis
import kotlinx.android.synthetic.main.fragment_app_launches.*

class AppLaunchesFragment : BaseFragment() {

    private lateinit var historyStatsViewModel: HistoryStatsViewModel
    private lateinit var appLaunchesViewModel: AppLaunchesViewModel

    override fun getLayoutId() = R.layout.fragment_app_launches

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModels()
    }

    private fun initViewModels() {
        historyStatsViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        appLaunchesViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[AppLaunchesViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        historyStatsViewModel.weeklyData.observe(this, Observer {
            appLaunchesViewModel.calculateWeeklyLaunches(it)
        })

        appLaunchesViewModel.barChartData.observe(this, Observer {
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

        appLaunchesViewModel.totalLaunchCount.observe(this, Observer {
            val avgPerDay = it / 7
            val avgPerHour = it / 7 / 24
            avgPerDayNumber.text = avgPerDay.toString()
            avgPerHourNumber.text = avgPerHour.toString()

            numberOfLaunchesTV.text = "$it app launches this week"
        })

        appLaunchesViewModel.mostOpenedApp.observe(this, Observer {
            Glide.with(context!!).load(PackageInfoUtils.getRawAppIcon(it.packageName, context)).into(mostOpenedAppIcon)
            mostOpenedTV.text = PackageInfoUtils.getAppName(it.packageName, context)
            mostOpenedNumberTV.text = "${it.launchCount} times"
        })
    }

}