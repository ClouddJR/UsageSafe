package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.adapters.AppLimitsAdapter
import com.clouddroid.usagesafe.viewmodels.AppLimitsViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_app_limits.*

class AppLimitsFragment : BaseFragment() {

    private lateinit var viewModel: AppLimitsViewModel
    private lateinit var adapter: AppLimitsAdapter

    override fun getLayoutId() = R.layout.fragment_app_limits

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV()
        observeData()

        systemAppsCB.setOnCheckedChangeListener { _, isChecked ->
            viewModel.getListOfApps(context!!, isChecked)
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[AppLimitsViewModel::class.java]
        viewModel.getListOfApps(context!!, false)
    }

    private fun initRV() {
        adapter = AppLimitsAdapter(
            mutableListOf(), context!!,
            { packageName -> showAppLimitDialog(packageName) }, //onAppLimitButtonClick
            { showScreenLimitDialog() }) // onScreenTimeLimitButtonClick

        appsListRV.adapter = adapter
    }

    private fun showAppLimitDialog(packageName: String) {
        val dialog = AlertDialog.Builder(context!!, R.style.AlertDialog)
            .setView(R.layout.dialog_app_limit)
            .show()

        val hourNumberPicker = dialog.findViewById<NumberPicker>(R.id.hourNumberPicker)
        hourNumberPicker?.minValue = 0
        hourNumberPicker?.maxValue = 23

        val minuteNumberPicker = dialog.findViewById<NumberPicker>(R.id.minuteNumberPicker)
        minuteNumberPicker?.minValue = 0
        minuteNumberPicker?.maxValue = 59

        dialog.findViewById<MaterialButton>(R.id.setLimitBT)?.setOnClickListener {
            viewModel.saveAppLimit(packageName, hourNumberPicker?.value, minuteNumberPicker?.value)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showScreenLimitDialog() {
        val dialog = AlertDialog.Builder(context!!, R.style.AlertDialog)
            .setView(R.layout.dialog_screen_limit)
            .show()

        val hourNumberPicker = dialog.findViewById<NumberPicker>(R.id.hourNumberPicker)
        hourNumberPicker?.minValue = 0
        hourNumberPicker?.maxValue = 23

        val minuteNumberPicker = dialog.findViewById<NumberPicker>(R.id.minuteNumberPicker)
        minuteNumberPicker?.minValue = 0
        minuteNumberPicker?.maxValue = 59

        dialog.findViewById<MaterialButton>(R.id.setLimitBT)?.setOnClickListener {
            viewModel.saveScreenLimit(hourNumberPicker?.value, minuteNumberPicker?.value)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun observeData() {
        viewModel.appsList.observe(this, Observer {
            adapter.swapItems(it)
        })
    }
}