package com.clouddroid.usagesafe.ui.todaystats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsActivity
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsActivity
import com.clouddroid.usagesafe.ui.main.MainActivity
import com.clouddroid.usagesafe.ui.settings.SettingsActivity
import com.clouddroid.usagesafe.util.purchase.PurchasesUtils.isPremiumUser
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.fragment_todays_stats.*
import java.text.SimpleDateFormat
import java.util.*


class TodaysStatsFragment : BaseFragment() {

    companion object {
        const val TAG = "TodaysStatsFragment"
    }

    private lateinit var viewModel: TodaysStatsViewModel
    private lateinit var appUsageInfoAdapter: MostUsedAdapter

    private lateinit var adLoader: AdLoader

    private var resumeCounter = 0

    override fun getLayoutId() = R.layout.fragment_todays_stats

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resumeCounter = 0
        observeDataChanges()
        setNoDataTextInChart()
        setOnArcClickListener()
        setCurrentDayInToolbar()
        setOnClickListeners()
        loadAd()
    }

    override fun onResume() {
        super.onResume()
        resumeCounter++
        showMoreBT.visibility = View.VISIBLE
        viewModel.init()
    }

    fun scrollToTop() {
        nestedScroll.smoothScrollTo(0, 0)
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(activity!!, viewModelFactory)[TodaysStatsViewModel::class.java]
    }

    private fun setNoDataTextInChart() {
        pieChart.setNoDataText("")
        pieChart.invalidate()
    }

    private fun setOnArcClickListener() {
        pieChart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureEnd(
                me: MotionEvent?,
                lpg: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartFling(
                me1: MotionEvent?,
                me2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) {
            }

            override fun onChartGestureStart(
                me: MotionEvent?,
                lpg: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
            override fun onChartLongPressed(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}

            override fun onChartSingleTapped(me: MotionEvent?) {
                me?.let {
                    val touchDistanceToCenter = pieChart.distanceToCenter(me.x, me.y)
                    if (touchDistanceToCenter <= (pieChart.radius - 20f)) {
                        startActivity(Intent(context, DayDetailsActivity::class.java))
                    } else {
                        var angle = pieChart.getAngleForPoint(me.x, me.y)
                        angle /= pieChart.animator.phaseY
                        val index = pieChart.getIndexForAngle(angle)
                        val entry = pieChart.data.dataSet.getEntryForIndex(index)

                        if ((entry as PieEntry).data != "") {
                            val intent = Intent(context, AppDetailsActivity::class.java)
                            intent.putExtra("package_name", entry.data as String)
                            startActivity(intent)
                        }
                    }

                }
            }
        }
    }

    private fun setCurrentDayInToolbar() {
        val formatter = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        val dateText = formatter.format(Date())
        dateTitle.text = dateText
    }

    private fun setOnClickListeners() {
        showMoreBT.setOnClickListener {
            appUsageInfoAdapter.addItems(viewModel.otherAppsList)
            showMoreBT.visibility = View.GONE
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }

        unlockCountTV.setOnClickListener {
            navigateTo(MainActivity.FragmentDestination.UNLOCKS)
        }

        unlockTV.setOnClickListener {
            navigateTo(MainActivity.FragmentDestination.UNLOCKS)
        }

        launchCountTV.setOnClickListener {
            navigateTo(MainActivity.FragmentDestination.APP_LAUNCHES)
        }

        launchTV.setOnClickListener {
            navigateTo(MainActivity.FragmentDestination.APP_LAUNCHES)
        }
    }

    private fun navigateTo(destination: MainActivity.FragmentDestination) {
        (activity as MainActivity).switchToFragment(destination)
    }

    private fun observeDataChanges() {
        viewModel.getAppUsageMap().observe(this, Observer {
            drawChart(it)
            setChartCenterText(viewModel.getTotalScreenTimeText(it, context))
            setUpMostUsedRV(viewModel.getMostUsedAppsList(it))
        })

        viewModel.unlockCount.observe(this, Observer {
            setUnlockText(it)
        })

        viewModel.launchCount.observe(this, Observer {
            setLaunchText(it)
        })
    }

    private fun drawChart(appUsageMap: Map<String, AppUsageInfo>) {
        val entries = viewModel.prepareEntriesForPieChart(appUsageMap, context)
        val pieDataSet = prepareDataSet(entries)
        fillAndCustomizeChart(pieDataSet)
    }

    private fun fillAndCustomizeChart(pieDataSet: PieDataSet) {
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.isHighlightPerTapEnabled = false
        pieChart.holeRadius = 90f
        pieChart.isRotationEnabled = false
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.extraBottomOffset = 20f
        pieChart.extraTopOffset = 20f
        pieChart.setCenterTextTypeface(ResourcesCompat.getFont(context!!, R.font.opensans_regular))
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.setCenterTextSize(16f)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setNoDataText("")
        pieChart.setHoleColor(Color.TRANSPARENT)
        if (resumeCounter == 1) {
            pieChart.animateXY(500, 500)
        } else {
            pieChart.invalidate()
        }
    }

    private fun prepareDataSet(entries: List<PieEntry>): PieDataSet {
        val pieDataSet = PieDataSet(entries, "App usage")
        pieDataSet.sliceSpace = 3f
        pieDataSet.colors = viewModel.generateColorsFromAppIcons(entries)
        pieDataSet.iconsOffset = MPPointF(0f, 32f)
        pieDataSet.valueTextSize = 0f
        pieDataSet.valueTypeface = ResourcesCompat.getFont(context!!, R.font.opensans_regular)
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.valueLinePart1OffsetPercentage = 0.5f
        pieDataSet.valueLinePart1Length = 0.15f
        pieDataSet.valueLinePart2Length = 0f
        pieDataSet.valueLineColor = Color.TRANSPARENT
        pieDataSet.setAutomaticallyDisableSliceSpacing(true)
        return pieDataSet
    }

    private fun setChartCenterText(totalScreenTimeText: String) {
        pieChart.centerText =
            "${context?.getString(R.string.fragment_today_total_screen_time)} \n$totalScreenTimeText"
    }

    private fun setUpMostUsedRV(list: MutableList<AppUsageInfo>) {
        appUsageInfoAdapter = MostUsedAdapter(list.toMutableList())
        mostUsedRV.adapter = appUsageInfoAdapter
        mostUsedRV.isNestedScrollingEnabled = false
    }

    private fun setUnlockText(unlockCount: Int) {
        unlockCountTV.text = unlockCount.toString()
    }

    private fun setLaunchText(launchCount: Int) {
        launchCountTV.text = launchCount.toString()
    }

    private fun loadAd() {
        if (!isPremiumUser(context)) {
            Handler().postDelayed({
                val builder =
                    AdLoader.Builder(context, getString(R.string.admob_today_stats_fragment_ad_id))
                adLoader = builder.forUnifiedNativeAd { unifiedNativeAd ->
                    //ad loaded successfully
                    if (!adLoader.isLoading && ::appUsageInfoAdapter.isInitialized) {
                        appUsageInfoAdapter.addAd(unifiedNativeAd)
                    }
                }.build()

                adLoader.loadAds(AdRequest.Builder().build(), 1)
            }, 500)
        }
    }
}