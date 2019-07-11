package com.clouddroid.usagesafe.ui.historystats.unlocks

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.LoadingState
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsActivity
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsViewModel
import com.clouddroid.usagesafe.ui.historystats.HistoryStatsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.fragment_unlocks.*

class UnlocksFragment : BaseFragment() {

    private lateinit var historyStatsViewModel: HistoryStatsViewModel
    private lateinit var unlocksViewModel: UnlocksViewModel

    override fun getLayoutId() = R.layout.fragment_unlocks

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    fun scrollToTop() {
        nestedScroll.smoothScrollTo(0, 0)
    }

    private fun initViewModels() {
        historyStatsViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[HistoryStatsViewModel::class.java]
        unlocksViewModel = ViewModelProviders.of(activity!!, viewModelFactory)[UnlocksViewModel::class.java]
    }

    private fun observeData() {
        historyStatsViewModel.weeklyData.observe(this, Observer {
            unlocksViewModel.calculateWeeklyUnlocks(it)
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

        unlocksViewModel.barChartData.observe(this, Observer {
            drawChart(it.first, it.second)
        })

        unlocksViewModel.totalUnlocks.observe(this, Observer {
            val avgPerDay = it / 7
            val avgPerHour = it / 7 / 24
            avgPerDayNumber.text = avgPerDay.toString()
            avgPerHourNumber.text = avgPerHour.toString()

            numberOfUnlocksTV.text = "$it screen unlocks this week"
        })
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
                intent.putExtra(DayDetailsActivity.MODE_KEY, DayDetailsViewModel.Mode.UNLOCKS)
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
            value.toInt().toString()
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