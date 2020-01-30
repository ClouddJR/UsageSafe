package com.clouddroid.usagesafe.ui.applimits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.service.AppUsageMonitorService
import com.clouddroid.usagesafe.ui.applimits.adddialog.AppLimitsDialog
import com.clouddroid.usagesafe.ui.applimits.focus.FocusAppsListDialog
import com.clouddroid.usagesafe.ui.applimits.help.HelpDialog
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.settings.SettingsActivity
import com.clouddroid.usagesafe.util.purchase.PurchasesUtils.isPremiumUser
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import kotlinx.android.synthetic.main.ad_app_limits_fragment.*
import kotlinx.android.synthetic.main.fragment_app_limits.*


class AppLimitsFragment : BaseFragment() {

    companion object {
        const val TAG = "AppLimitsFragment"
    }

    private lateinit var viewModel: AppLimitsViewModel
    private lateinit var adapter: AppLimitsAdapter

    private lateinit var adLoader: AdLoader

    override fun getLayoutId() = R.layout.fragment_app_limits

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
        adapter = AppLimitsAdapter(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV()
        observeData()
        setOnClickListeners()
        manageFocusModeToggle()
        initAdView()
        loadAd()
    }

    fun scrollToTop() {
        nestedScroll.smoothScrollTo(0, 0)
        appsListRV.smoothScrollToPosition(0)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[AppLimitsViewModel::class.java]
        viewModel.init()
    }

    private fun initRV() {
        val animation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        appsListRV.adapter = adapter
        appsListRV.layoutAnimation = animation
        appsListRV.scheduleLayoutAnimation()
        appsListRV.invalidate()
    }

    private fun observeData() {
        viewModel.usageMap.observe(this, Observer {
            adapter.updateUsageProgressBars(it)
        })

        viewModel.appsList.observe(this, Observer {
            updateItems(it)
            if (isListEmptyAndFocusModeDisabled(it)) {
                stopUsageMonitorService()
            } else {
                startUsageMonitorService(null)
            }
        })

        viewModel.isFocusModeEnabled.observe(this, Observer { isEnabled ->
            focusModeSwitch.isChecked = isEnabled
            if (isAdapterDataEmptyAndFocusModeDisabled(isEnabled)) {
                stopUsageMonitorService()
            } else {
                startUsageMonitorService(isEnabled)
            }
        })
    }

    private fun updateItems(appsList: List<AppLimit>) {
        adapter.replaceItems(appsList)
    }

    private fun isListEmptyAndFocusModeDisabled(list: List<AppLimit>): Boolean =
        list.isEmpty() && !focusModeSwitch.isChecked

    private fun isAdapterDataEmptyAndFocusModeDisabled(isEnabled: Boolean): Boolean =
        adapter.itemCount == 0 && !isEnabled

    private fun stopUsageMonitorService() {
        val intent = Intent(context, AppUsageMonitorService::class.java)
        context?.stopService(intent)
    }

    private fun startUsageMonitorService(data: Boolean?) {
        val intent = Intent(context, AppUsageMonitorService::class.java)
        intent.putExtra(AppUsageMonitorService.FOCUS_MODE_KEY, data)
        ContextCompat.startForegroundService(context!!, intent)
    }

    private fun setOnClickListeners() {
        addLimitFAB.setOnClickListener {
            fragmentManager?.let {
                AppLimitsDialog.display(it)
            }
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }

        openFocusAppsListBT.setOnClickListener {
            fragmentManager?.let {
                FocusAppsListDialog.display(it)
            }
        }

        focusHelpBT.setOnClickListener {
            val dialog = HelpDialog(context!!, HelpDialog.HelpForm.FOCUS)
            dialog.show()
        }

        appLimitsHelpBT.setOnClickListener {
            val dialog = HelpDialog(context!!, HelpDialog.HelpForm.APP_LIMITS)
            dialog.show()
        }
    }

    private fun manageFocusModeToggle() {
        focusModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFeatureState(isChecked)
        }
    }

    private fun initAdView() {
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.priceView = adView.findViewById(R.id.adPrice)
        adView.starRatingView = adView.findViewById(R.id.adStars)
        adView.storeView = adView.findViewById(R.id.adStore)
        adView.advertiserView = adView.findViewById(R.id.adAdvertiser)
    }

    private fun loadAd() {
        if (!isPremiumUser(context)) {
            try {
                val builder = AdLoader.Builder(context, getString(R.string.admob_app_limits_fragment_ad_id))
                adLoader = builder.forUnifiedNativeAd { unifiedNativeAd ->
                    //ad loaded successfully
                    if (!adLoader.isLoading) {
                        if (adSection != null) {
                            adSection.visibility = View.VISIBLE
                            populateNativeAdView(unifiedNativeAd)
                        }
                    }
                }.withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(errorCode: Int) {
                            //ad failed to load, so hide ad section
                            if (adSection != null) {
                                adSection.visibility = View.GONE
                            }
                        }
                    }).build()

                adLoader.loadAds(AdRequest.Builder().build(), 1)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun populateNativeAdView(nativeAd: UnifiedNativeAd) {
        // These assets are guaranteed to be in every UnifiedNativeAd
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction

        // These assets aren't guaranteed to be in every UnifiedNativeAd
        val icon = nativeAd.icon

        if (icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }
}
