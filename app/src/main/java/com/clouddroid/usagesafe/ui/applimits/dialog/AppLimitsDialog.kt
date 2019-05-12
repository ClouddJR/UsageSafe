package com.clouddroid.usagesafe.ui.applimits.dialog

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseDialogFragment
import com.clouddroid.usagesafe.util.ExtensionUtils.showAndHideOthers
import kotlinx.android.synthetic.main.dialog_add_app_limit.*

class AppLimitsDialog : BaseDialogFragment() {

    private val appListFragment = AppListFragment()
    private val limitFragment = LimitFragment()

    private lateinit var viewModel: AppLimitsDialogViewModel

    companion object {
        private val TAG = AppLimitsDialog::class.java.name

        fun display(fragmentManager: FragmentManager): AppLimitsDialog {
            val dialog = AppLimitsDialog()
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onResume() {
        super.onResume()
        overrideBackButtonPress()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(R.layout.dialog_add_app_limit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()
        initFragments()
        observeDataChanges()
    }

    override fun onStart() {
        super.onStart()
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setWindowAnimations(R.style.SlideAnimation)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AppLimitsDialogViewModel::class.java]
    }

    private fun overrideBackButtonPress() {
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (limitFragment.isVisible) {
                    replaceFragment(FragmentDestination.APP_LIST)
                } else {
                    dismiss()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setUpToolbar() {
        toolbar.title = "Choose app"
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.setOnMenuItemClickListener {
            viewModel.saveAppLimit()
            dismiss()
            true
        }
    }

    private fun initFragments() {
        childFragmentManager.beginTransaction()
            .add(R.id.fragmentPlaceHolder, appListFragment)
            .add(R.id.fragmentPlaceHolder, limitFragment)
            .hide(limitFragment)
            .commit()
    }

    private fun observeDataChanges() {
        viewModel.selectedApp.observe(this, Observer {
            replaceFragment(FragmentDestination.LIMIT)
        })
    }

    private fun replaceFragment(destination: FragmentDestination) {
        val transaction = childFragmentManager.beginTransaction()

        when (destination) {
            FragmentDestination.APP_LIST -> {
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                transaction.showAndHideOthers(appListFragment, childFragmentManager.fragments)
                updateDialogToolbarAccordingTo(FragmentDestination.APP_LIST)
            }

            FragmentDestination.LIMIT -> {
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                transaction.showAndHideOthers(limitFragment, childFragmentManager.fragments)
                updateDialogToolbarAccordingTo(FragmentDestination.LIMIT)
            }
        }
        transaction.commit()
    }

    private fun updateDialogToolbarAccordingTo(destination: FragmentDestination) {
        when (destination) {
            FragmentDestination.APP_LIST -> {
                toolbar.menu.clear()
                toolbar.title = "Choose app"
            }

            FragmentDestination.LIMIT -> {
                toolbar.menu.clear()
                toolbar.inflateMenu(R.menu.dialog_app_limit_menu)
                toolbar.title = "Set a limit"
            }
        }
    }

    enum class FragmentDestination {
        APP_LIST,
        LIMIT
    }
}
