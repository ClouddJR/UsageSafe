package com.clouddroid.usagesafe.ui.applimits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.service.AppUsageMonitorService
import com.clouddroid.usagesafe.ui.applimits.dialog.AppLimitsDialog
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.ui.settings.SettingsActivity
import io.realm.OrderedRealmCollection
import kotlinx.android.synthetic.main.fragment_app_limits.*

class AppLimitsFragment : BaseFragment() {

    private lateinit var viewModel: AppLimitsViewModel
    private lateinit var adapter: AppLimitsAdapter

    private var viewPropertyAnimator: ViewPropertyAnimator? = null

    override fun getLayoutId() = R.layout.fragment_app_limits

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setOnClickListeners()
        manageFeatureToggle()
    }

    fun scrollToTop() {
        nestedScroll.smoothScrollTo(0, 0)
        appsListRV.smoothScrollToPosition(0)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[AppLimitsViewModel::class.java]
        viewModel.init()
    }

    private fun observeData() {
        viewModel.appsList.observe(this, Observer {
            initRV(it)
        })

        viewModel.areAppLimitsEnabled.observe(this, Observer { isEnabled ->
            toggleUsageMonitorService(isEnabled)
            enableSwitch.isChecked = isEnabled

            //toggle the RV visibility and schedule items animation if visible
            if (isEnabled) {
                if (viewPropertyAnimator != null) {
                    viewPropertyAnimator!!.cancel()
                }
                appsListRV.alpha = 1f
                appsListRV.visibility = View.VISIBLE
                val animation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
                appsListRV.layoutAnimation = animation
                appsListRV.scheduleLayoutAnimation()
                appsListRV.invalidate()

            } else {
                viewPropertyAnimator = appsListRV.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { appsListRV.visibility = View.INVISIBLE }
                viewPropertyAnimator?.start()
            }
        })
    }

    private fun toggleUsageMonitorService(isEnabled: Boolean) {
        val intent = Intent(context, AppUsageMonitorService::class.java)

        //stopping or launching service that monitors usage
        when (isEnabled) {
            true -> ContextCompat.startForegroundService(context!!, intent)
            false -> context?.stopService(intent)
        }

    }

    private fun initRV(appsList: List<AppLimit>) {
        adapter = AppLimitsAdapter(
            appsList as OrderedRealmCollection<AppLimit>,
            context!!
        )

        appsListRV.adapter = adapter
    }

    private fun setOnClickListeners() {
        addLimitFAB.setOnClickListener {
            displayDialog()
        }

        settingsIcon.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    private fun displayDialog() {
        fragmentManager?.let {
            AppLimitsDialog.display(it)
        }
    }

    private fun manageFeatureToggle() {
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFeatureState(isChecked)
        }
    }
}
