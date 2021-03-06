package com.clouddroid.usagesafe.ui.applimits.adddialog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppDetails
import com.clouddroid.usagesafe.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_app_list_in_dialog.*

class AppListFragment : BaseFragment() {

    private lateinit var viewModel: AppLimitsDialogViewModel
    private lateinit var adapter: AppListAdapter

    override fun getLayoutId() = R.layout.fragment_app_list_in_dialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRVAdapter()
        observeDataChanges()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(parentFragment!!, viewModelFactory)[AppLimitsDialogViewModel::class.java]
        viewModel.getListOfAllApps(context!!)
    }

    private fun initRVAdapter() {
        adapter = AppListAdapter(context!!) { packageName ->
            viewModel.setAppClicked(packageName)
        }

        appsRV.layoutManager = LinearLayoutManager(context)
        appsRV.adapter = adapter
    }

    private fun observeDataChanges() {
        viewModel.appsList.observe(this, Observer {
            updateRVItems(it)
        })
    }

    private fun updateRVItems(appDetails: List<AppDetails>) {
        adapter.swapItems(appDetails)
    }
}