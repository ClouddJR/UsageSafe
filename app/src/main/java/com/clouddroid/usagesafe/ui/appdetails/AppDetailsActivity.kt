package com.clouddroid.usagesafe.ui.appdetails

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseActivity
import com.clouddroid.usagesafe.data.model.LoadingState
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.ui.common.SpinnerAdapter
import com.clouddroid.usagesafe.util.TextUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import kotlinx.android.synthetic.main.activity_app_details.*
import kotlin.math.roundToInt

class AppDetailsActivity : BaseActivity() {

    private lateinit var appPackageName: String
    private lateinit var viewModel: AppDetailsViewModel

    companion object {
        const val PACKAGE_NAME_KEY = "package_name"
        const val MODE_KEY = "details_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_details)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        barChart.setNoDataText("")
        barChart.invalidate()

        receivePackageName()
        setUpSpinner()
        initViewModel()
        observeData()
        setOnWeekChangeListener()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun receivePackageName() {
        appPackageName = intent.getStringExtra(PACKAGE_NAME_KEY)
        appTitleTV.text = PackageInfoUtils.getAppName(appPackageName, this)
        Glide.with(this).load(PackageInfoUtils.getRawAppIcon(appPackageName, this)).into(appIconIV)
    }

    private fun setUpSpinner() {
        val adapter = SpinnerAdapter(
            this, R.layout.spinner_list_item,
            arrayOf("Screen time", "App launches")
        )

        statsTypeSpinner.adapter = adapter

        statsTypeSpinner.post {
            statsTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        AppDetailsViewModel.MODE.MODE_SCREEN_TIME ->
                            viewModel.switchMode(AppDetailsViewModel.MODE.MODE_SCREEN_TIME)
                        AppDetailsViewModel.MODE.MODE_APP_LAUNCHES ->
                            viewModel.switchMode(AppDetailsViewModel.MODE.MODE_APP_LAUNCHES)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AppDetailsViewModel::class.java]

        val passedMode = intent.getIntExtra(MODE_KEY, 0)
        viewModel.currentMode = passedMode

        viewModel.init(appPackageName)
        viewModel.updateCurrentWeek()
    }

    private fun observeData() {

        viewModel.currentWeekText.observe(this, Observer {
            dateTV.text = it
        })

        viewModel.weeklyData.observe(this, Observer {
            viewModel.calculateWeeklyUsage(it)
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

        viewModel.loadingState.observe(this, Observer {
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

        viewModel.barChartData.observe(this, Observer {
            drawChart(it.first, it.second)
        })

        viewModel.totalScreenTimeLiveData.observe(this, Observer {
            val avgPerDay = it / 7
            val avgPerHour = it / 7 / 24
            avgPerDayNumber.text = TextUtils.getTotalScreenTimeText(avgPerDay, this)
            avgPerHourNumber.text = TextUtils.getTotalScreenTimeText(avgPerHour, this)

            weeklyUsageSummaryTV.text = "${TextUtils.getTotalScreenTimeText(it, this)} of usage this week"
        })

        viewModel.totalLaunchCountLiveData.observe(this, Observer {
            val avgPerDay = (it / 7.0).roundToInt()
            val avgPerHour = (it / 7.0 / 24.0).roundToInt()
            avgPerDayNumber.text = avgPerDay.toString()
            avgPerHourNumber.text = avgPerHour.toString().format("%.2")

            weeklyUsageSummaryTV.text = "$it app launches this week"
        })
    }

    private fun drawChart(barDataSet: BarDataSet, daysNames: List<String>) {
        barDataSet.valueTypeface = ResourcesCompat.getFont(this, R.font.opensans_regular)
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)

        barChart.data = BarData(barDataSet)
        barChart.data.setValueTextSize(9f)

        if (viewModel.currentMode == AppDetailsViewModel.MODE.MODE_SCREEN_TIME) {
            barChart.data.setValueFormatter { value, _, _, _ ->
                TextUtils.getTotalScreenTimeText(value.toLong(), this)
            }
        } else {
            barChart.data.setValueFormatter { value, _, _, _ ->
                value.toInt().toString()
            }
        }

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
            daysNames[value.toInt()]
        }

        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisLeft.setDrawLabels(false)
        barChart.axisLeft.typeface = ResourcesCompat.getFont(this, R.font.opensans_regular)
        barChart.axisLeft.textColor = Color.WHITE


        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)
        barChart.animateXY(500, 400)
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