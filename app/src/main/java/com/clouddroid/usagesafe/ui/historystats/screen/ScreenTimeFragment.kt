package com.clouddroid.usagesafe.ui.historystats.screen

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.LoadingState
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsActivity
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsViewModel
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsActivity
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsViewModel
import com.clouddroid.usagesafe.ui.historystats.HistoryStatsViewModel
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.TextUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
        setOnClickListeners()
    }

    fun scrollToTop() {
        nestedScroll.smoothScrollTo(0, 0)
    }

    private fun initViewModels() {
        historyStatsViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        screenTimeViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[ScreenTimeViewModel::class.java]
    }

    private fun observeData() {
        historyStatsViewModel.weeklyData.observe(this, Observer {
            screenTimeViewModel.calculateWeeklyUsage(it)
        })

        historyStatsViewModel.loadingState.observe(this, Observer {
            when (it) {
                LoadingState.FINISHED -> {
                    barChart.visibility = View.VISIBLE
                    loadingView.smoothToHide()
                }
                LoadingState.LOADING -> {
                    barChart.visibility = View.INVISIBLE
                    loadingView.smoothToShow()
                }
                else -> {
                }
            }
        })

        screenTimeViewModel.barChartData.observe(this, Observer {
            drawChart(it.first, it.second)
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

            val usageText = "${TextUtils.getTotalScreenTimeText(
                it,
                context
            )} ${getString(R.string.fragment_screen_time_of_usage_this_week)}"
            weeklyUsageSummaryTV.text = usageText
        })
    }

    private fun setOnClickListeners() {
        mostUsedAppIcon.setOnClickListener(onMostUsedAppClick)
        mostUsedTV.setOnClickListener(onMostUsedAppClick)
        mostUsedTimeTV.setOnClickListener(onMostUsedAppClick)
    }

    private val onMostUsedAppClick = View.OnClickListener {
        val intent = Intent(context, AppDetailsActivity::class.java)
        intent.putExtra(AppDetailsActivity.PACKAGE_NAME_KEY, screenTimeViewModel.mostUsedApp.value?.packageName)
        intent.putExtra(AppDetailsActivity.MODE_KEY, AppDetailsViewModel.Mode.SCREEN_TIME)
        startActivity(intent)
    }

    private fun drawChart(barDataSet: BarDataSet, daysNames: List<String>) {
        resetChart()
        barDataSet.valueTypeface = ResourcesCompat.getFont(context!!, R.font.opensans_regular)
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {

            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val intent = Intent(context, DayDetailsActivity::class.java)
                intent.putExtra(DayDetailsActivity.DATE_MILLIS_KEY, (e as BarEntry).data as Long)
                intent.putExtra(DayDetailsActivity.MODE_KEY, DayDetailsViewModel.Mode.SCREEN_TIME)
                startActivity(intent)
            }
        })

        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.isDragEnabled = false
        barChart.description.isEnabled = false
        barChart.isDoubleTapToZoomEnabled = false
        barChart.legend.isEnabled = false
        barChart.extraBottomOffset = 8f

        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.textSize = 13f
        barChart.xAxis.textColor = Color.WHITE
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.labelCount = daysNames.size
        barChart.xAxis.setValueFormatter { value, _ ->
            daysNames[value.toInt()]
        }

        barChart.data = BarData(barDataSet)
        barChart.data.setValueFormatter { value, _, _, _ ->
            TextUtils.getTotalScreenTimeText(value.toLong(), context)
        }
        barChart.data.setValueTextSize(9f)

        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisLeft.setDrawLabels(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)
        barChart.animateXY(500, 400)
    }

    private fun resetChart() {
        barChart.data?.clearValues()
        barChart.xAxis.valueFormatter = null
        barChart.notifyDataSetChanged()
    }
}