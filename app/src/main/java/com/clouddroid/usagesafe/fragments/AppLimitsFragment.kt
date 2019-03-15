package com.clouddroid.usagesafe.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.adapters.AppLimitsAdapter
import com.clouddroid.usagesafe.viewmodels.AppLimitsViewModel
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
        observeDataChanges()
    }

    private fun initRV() {
        adapter = AppLimitsAdapter(mutableListOf(), context!!)
        appsListRV.adapter = adapter
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[AppLimitsViewModel::class.java]
        viewModel.init(context!!)
    }

    private fun observeDataChanges() {
        viewModel.appsList.observe(this, Observer {
            adapter.setItems(it)
        })
    }
}