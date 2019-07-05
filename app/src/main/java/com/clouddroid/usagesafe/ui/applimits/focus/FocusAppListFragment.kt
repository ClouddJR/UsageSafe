package com.clouddroid.usagesafe.ui.applimits.focus

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_app_list_in_dialog.*

class FocusAppListFragment : BaseFragment() {

    private lateinit var viewModel: FocusAppListViewModel
    private lateinit var adapter: FocusAppListAdapter

    override fun getLayoutId() = R.layout.fragment_app_list_in_dialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDataChanges()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FocusAppListViewModel::class.java]
        viewModel.getAdapterData(context!!)
    }

    private fun observeDataChanges() {
        viewModel.adapterData.observe(this, Observer {
            adapter = FocusAppListAdapter(it.first, it.second) { app ->
                //checkBox clicked
                viewModel.addOrRemoveAppFromFocusAppsList(app)
            }
            appsRV.layoutManager = LinearLayoutManager(context)
            appsRV.adapter = adapter
        })
    }
}