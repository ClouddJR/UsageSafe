package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.clouddroid.usagesafe.adapters.MostUsedAdapter
import com.clouddroid.usagesafe.models.AppUsageInfo
import com.clouddroid.usagesafe.viewmodels.TodaysStatsViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.fragment_todays_stats.*


class TodaysStatsFragment : BaseFragment() {

    private lateinit var viewModel: TodaysStatsViewModel

    override fun getLayoutId() = com.clouddroid.usagesafe.R.layout.fragment_todays_stats

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init()
        observeDataChanges()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[TodaysStatsViewModel::class.java]
    }

    private fun observeDataChanges() {
        viewModel.getAppUsageMap().observe(this, Observer {
            drawChart(it)
            setChartCenterText(viewModel.getTotalScreenTime(it))
            setUpMostUsedRV(viewModel.getMostUsedAppsList(it))
        })

        viewModel.unlockCount.observe(this, Observer {
            setUnlockText(it)
        })
    }

    private fun setUpMostUsedRV(list: List<AppUsageInfo>) {
        mostUsedRV.adapter = MostUsedAdapter(list)
        val dividerItemDecoration = DividerItemDecoration(
            mostUsedRV.context, LinearLayoutManager.VERTICAL
        )
        mostUsedRV.addItemDecoration(dividerItemDecoration)
    }

    private fun setUnlockText(unlockCount: Int?) {
        unlockCountTV.text = unlockCount.toString()
    }

    private fun setChartCenterText(totalScreenTimeText: String) {
        pieChart.centerText = "Total screen time: \n$totalScreenTimeText"
    }

    private fun drawChart(appUsageMap: Map<String, AppUsageInfo>) {
        val entries = viewModel.prepareEntriesForPieChart(appUsageMap, context)
        val pieDataSet = prepareDataSet(entries)
        fillAndCustomizeChart(pieDataSet)
    }


    private fun prepareDataSet(entries: List<PieEntry>): PieDataSet {
        val pieDataSet = PieDataSet(entries, "App usage")
        pieDataSet.sliceSpace = 3f
        pieDataSet.colors = viewModel.generateColorsFromAppIcons(entries)
        pieDataSet.isHighlightEnabled = false
        pieDataSet.iconsOffset = MPPointF(0f, 32f)
        pieDataSet.valueTextSize = 0f
        pieDataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.valueLinePart1OffsetPercentage = 0.5f
        pieDataSet.valueLinePart1Length = 0.15f
        pieDataSet.valueLinePart2Length = 0f
        pieDataSet.valueLineColor = Color.TRANSPARENT
        pieDataSet.setAutomaticallyDisableSliceSpacing(true)
        return pieDataSet
    }

    private fun fillAndCustomizeChart(pieDataSet: PieDataSet) {
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.holeRadius = 90f
        pieChart.isRotationEnabled = false
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.extraBottomOffset = 20f
        pieChart.extraTopOffset = 20f
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.setCenterTextSize(16f)
        pieChart.animateXY(500, 500)
        pieChart.invalidate()
    }
}