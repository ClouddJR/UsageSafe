package com.clouddroid.usagesafe.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.clouddroid.usagesafe.UsageSafeApp
import javax.inject.Inject

abstract class BaseFragment : Fragment() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectDependencies()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    private fun injectDependencies() {
        (activity?.application as UsageSafeApp).component.inject(this)
    }

    abstract fun getLayoutId(): Int
}