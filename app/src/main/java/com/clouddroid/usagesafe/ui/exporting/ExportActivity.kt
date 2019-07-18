package com.clouddroid.usagesafe.ui.exporting

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseActivity
import com.clouddroid.usagesafe.util.ExtensionUtils.hide
import com.clouddroid.usagesafe.util.ExtensionUtils.show
import kotlinx.android.synthetic.main.activity_export.*
import org.jetbrains.anko.longToast

class ExportActivity : BaseActivity() {

    private lateinit var viewModel: ExportActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        setContentView(R.layout.activity_export)
        setOnButtonClickListeners()
        observeChanges()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[ExportActivityViewModel::class.java]
    }

    private fun setOnButtonClickListeners() {
        firstLayoutExportButton.setOnClickListener {
            firstLayoutProgressBar.show()
            it.hide()
            viewModel.exportData(Exporter.Layout.ONE_TABLE, this)
        }
        secondLayoutExportButton.setOnClickListener {
            secondLayoutProgressBar.show()
            it.hide()
            viewModel.exportData(Exporter.Layout.MULTIPLE_TABLES, this)
        }
    }

    private fun observeChanges() {
        viewModel.dataSuccessfullySaved.observe(this, Observer { path ->
            dismissProgressDialogIfShown()
            longToast("${getString(R.string.activity_export_file_saved)} $path")
        })

        viewModel.pathNotAvailable.observe(this, Observer {
            dismissProgressDialogIfShown()
            longToast(getString(R.string.activity_export_storage_not_available))
        })
    }

    private fun dismissProgressDialogIfShown() {
        firstLayoutProgressBar.hide()
        secondLayoutProgressBar.hide()
        firstLayoutExportButton.show()
        secondLayoutExportButton.show()
    }

}