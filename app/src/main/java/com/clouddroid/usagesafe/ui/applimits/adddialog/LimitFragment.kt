package com.clouddroid.usagesafe.ui.applimits.adddialog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.clouddroid.usagesafe.util.PackageInfoUtils
import kotlinx.android.synthetic.main.fragment_set_limit_in_dialog.*

class LimitFragment : BaseFragment() {

    private lateinit var viewModel: AppLimitsDialogViewModel

    override fun getLayoutId() = R.layout.fragment_set_limit_in_dialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpNumberPickers()
        observeSelectedApp()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)[AppLimitsDialogViewModel::class.java]
        viewModel.getListOfAllApps(context!!)
    }

    private fun setUpNumberPickers() {
        hourNumberPicker.minValue = 0
        hourNumberPicker.maxValue = 23

        hourNumberPicker.setOnValueChangedListener { _, _, value ->
            viewModel.updateHourPickerValue(value)
        }

        minuteNumberPicker.minValue = 0
        minuteNumberPicker.maxValue = 59

        minuteNumberPicker.setOnValueChangedListener { _, _, value ->
            viewModel.updateMinutePickerValue(value)
        }
    }

    private fun observeSelectedApp() {
        viewModel.selectedApp.observe(this, Observer { packageName ->
            adHeadline.text = PackageInfoUtils.getAppName(packageName, context)
            Glide.with(this).load(PackageInfoUtils.getRawAppIcon(packageName, context)).into(adIcon)
        })
    }
}