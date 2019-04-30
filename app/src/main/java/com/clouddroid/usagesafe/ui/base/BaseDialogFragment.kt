package com.clouddroid.usagesafe.ui.base

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.clouddroid.usagesafe.UsageSafeApp
import javax.inject.Inject

abstract class BaseDialogFragment : DialogFragment() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectDependencies()
    }

    private fun injectDependencies() {
        (activity?.application as UsageSafeApp).component.inject(this)
    }
}