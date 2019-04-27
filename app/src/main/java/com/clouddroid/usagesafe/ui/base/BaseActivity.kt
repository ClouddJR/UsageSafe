package com.clouddroid.usagesafe.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.clouddroid.usagesafe.UsageSafeApp
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    private fun injectDependencies() {
        (application as UsageSafeApp).component.inject(this)
    }

}