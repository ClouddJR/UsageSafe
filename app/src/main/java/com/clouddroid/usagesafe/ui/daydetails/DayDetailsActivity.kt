package com.clouddroid.usagesafe.ui.daydetails

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseActivity
import com.clouddroid.usagesafe.ui.common.HourMarkerView
import com.clouddroid.usagesafe.ui.common.SpinnerAdapter
import com.clouddroid.usagesafe.util.TextUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import kotlinx.android.synthetic.main.activity_day_details.*
import java.math.RoundingMode

class DayDetailsActivity : BaseActivity() {

    private lateinit var viewModel: DayDetailsViewModel
    private lateinit var adapter: DayDetailsAppsAdapter

    companion object {
        const val DATE_MILLIS_KEY = "day_time_millis"
        const val MODE_KEY = "details_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_details)
        initViewModel()
        setUpToolbar()
        setNoDataTextInChart()
        setUpSpinner()
        initRV()
        observeData()
        setupOnDayChangeListener()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DayDetailsViewModel::class.java]
        val passedDayTimeInMillis = intent.getLongExtra(DATE_MILLIS_KEY, 0L)
        if (passedDayTimeInMillis > 0L) {
            viewModel.setCurrentDay(passedDayTimeInMillis)
        }

        val passedMode = intent.getIntExtra(MODE_KEY, 0)
        viewModel.currentMode = passedMode
        viewModel.updateCurrentDay()
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setNoDataTextInChart() {
        barChart.setNoDataText("")
        barChart.invalidate()
    }

    private fun setUpSpinner() {
        val adapter = SpinnerAdapter(
            this, R.layout.spinner_list_item,
            arrayOf("Screen time", "App launches", "Unlocks")
        )

        statsTypeSpinner.adapter = adapter
        statsTypeSpinner.setSelection(viewModel.currentMode)

        statsTypeSpinner.post {
            statsTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        DayDetailsViewModel.MODE.SCREEN_TIME ->
                            viewModel.switchMode(DayDetailsViewModel.MODE.SCREEN_TIME)
                        DayDetailsViewModel.MODE.APP_LAUNCHES ->
                            viewModel.switchMode(DayDetailsViewModel.MODE.APP_LAUNCHES)
                        DayDetailsViewModel.MODE.UNLOCKS ->
                            viewModel.switchMode(DayDetailsViewModel.MODE.UNLOCKS)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

    }

    private fun initRV() {
        adapter = DayDetailsAppsAdapter(mutableListOf())
        appUsageRV.adapter = adapter
    }

    private fun observeData() {
        viewModel.barChartData.observe(this, Observer {
            drawChart(it.first, it.second)
        })

        viewModel.totalScreenTimeLiveData.observe(this, Observer {
            val avgPerHour = it / 24
            avgPerHourNumber.text = TextUtils.getTotalScreenTimeText(avgPerHour, this)

            dailyUsageSummaryTV.text = "${TextUtils.getTotalScreenTimeText(it, this)} of usage today"
        })

        viewModel.totalLaunchCountLiveData.observe(this, Observer {
            val avgPerHour = (it / 24.0).toBigDecimal().setScale(2, RoundingMode.UP)
            avgPerHourNumber.text = "$avgPerHour"

            dailyUsageSummaryTV.text = "$it app launches today"
        })

        viewModel.totalUnlocksLiveData.observe(this, Observer {
            val avgPerHour = (it / 24.0).toBigDecimal().setScale(2, RoundingMode.UP)
            avgPerHourNumber.text = "$avgPerHour"

            dailyUsageSummaryTV.text = "$it unlocks today"
        })

        viewModel.getAppsUsageList().observe(this, Observer {
            adapter.swapItems(it)
        })

        viewModel.currentDayText.observe(this, Observer {
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

    private fun drawChart(barDataSet: BarDataSet, hoursNames: List<String>) {
        barDataSet.valueTypeface = ResourcesCompat.getFont(this, R.font.opensans_regular)
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)

        barChart.data = BarData(barDataSet)
        barChart.data.setDrawValues(false)
        barChart.data.setValueTextSize(9f)

        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.extraBottomOffset = 8f

        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.textSize = 13f
        barChart.xAxis.textColor = Color.WHITE
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setValueFormatter { value, _ ->
            hoursNames[value.toInt()]
        }

        val marker = HourMarkerView(
            this,
            R.layout.chart_marker_view,
            viewModel.currentMode,
            hoursNames
        )
        marker.chartView = barChart
        barChart.marker = marker

        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisLeft.typeface = ResourcesCompat.getFont(this, R.font.opensans_regular)
        barChart.axisLeft.textColor = Color.WHITE
        barChart.axisLeft.setDrawAxisLine(true)
        barChart.axisLeft.setDrawLabels(true)
        if (viewModel.currentMode == DayDetailsViewModel.MODE.SCREEN_TIME) {
            barChart.axisLeft.setValueFormatter { value, _ ->
                TextUtils.getTotalScreenTimeText(value.toLong(), this)
            }
        } else {
            barChart.axisLeft.setValueFormatter { value, _ ->
                value.toInt().toString()
            }
        }

        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)

        barChart.animateXY(500, 400)
    }

    private fun setupOnDayChangeListener() {
        arrowRight.setOnClickListener {
            viewModel.rightArrowClicked()
        }

        arrowLeft.setOnClickListener {
            viewModel.leftArrowClicked()
        }
    }
}
