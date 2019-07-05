package com.clouddroid.usagesafe.ui.applimits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.service.AppUsageMonitorService
import com.clouddroid.usagesafe.ui.applimits.adddialog.AppLimitsDialog
import com.clouddroid.usagesafe.ui.applimits.focus.FocusAppsListDialog
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_app_limits.*

class AppLimitsFragment : BaseFragment() {

    private lateinit var viewModel: AppLimitsViewModel
    private lateinit var adapter: AppLimitsAdapter

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
    }

    private fun manageFocusModeToggle() {
        focusModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFeatureState(isChecked)
        }
    }
}
