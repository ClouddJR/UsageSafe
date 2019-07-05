package com.clouddroid.usagesafe.ui.applimits.focus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_add_app_limit.*

class FocusAppsListDialog : BaseDialogFragment() {

    private val appListFragment = FocusAppListFragment()

    companion object {
        @JvmStatic
        private val TAG = FocusAppsListDialog::class.java.name

        @JvmStatic
        fun display(fragmentManager: FragmentManager): FocusAppsListDialog {
            val dialog = FocusAppsListDialog()
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(R.layout.dialog_add_app_limit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        initFragments()
    }

    override fun onStart() {
        super.onStart()
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setWindowAnimations(R.style.SlideAnimation)
    }

    private fun setUpToolbar() {
        toolbar.title = "Select apps for focus mode"
        toolbar.setNavigationOnClickListener { dismiss() }
    }

    private fun initFragments() {
        childFragmentManager.beginTransaction()
            .add(R.id.fragmentPlaceHolder, appListFragment)
            .commit()
    }
}