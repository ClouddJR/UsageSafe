package com.clouddroid.usagesafe.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_permission.*
import org.jetbrains.anko.longToast


class PermissionActivity : BaseActivity() {

    private lateinit var viewModel: PermissionActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        initViewModel()
        setOnClickListeners()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[PermissionActivityViewModel::class.java]
    }

    private fun setOnClickListeners() {
        permissionButton.setOnClickListener {
            viewModel.startWatchingForPermissionChanges()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            longToast("Find UsageSafe here and grant the permission")
        }
    }
}