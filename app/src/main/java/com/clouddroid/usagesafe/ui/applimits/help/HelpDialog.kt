package com.clouddroid.usagesafe.ui.applimits.help

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.clouddroid.usagesafe.R
import kotlinx.android.synthetic.main.dialog_help.*

class HelpDialog(context: Context, private val helpForm: HelpForm) : Dialog(context) {

    enum class HelpForm {
        FOCUS,
        APP_LIMITS
    }

    private val focusHelpTitle = context.getString(R.string.dialog_help_focus_mode_title)
    private val focusHelpText = context.getString(R.string.dialog_help_focus_mode_description)

    private val appLimitsHelpTitle = context.getString(R.string.dialog_help_app_limits_mode_title)
    private val appLimitsHelpText = context.getString(R.string.dialog_help_app_limits_mode_description)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_help)
        setTransparentBackground()
        setContextText()
        setOnClickListeners()
    }

    private fun setTransparentBackground() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setContextText() {
        helpText.text = when (helpForm) {
            HelpForm.FOCUS -> focusHelpText
            HelpForm.APP_LIMITS -> appLimitsHelpText
        }

        titleText.text = when (helpForm) {
            HelpForm.FOCUS -> focusHelpTitle
            HelpForm.APP_LIMITS -> appLimitsHelpTitle
        }
    }

    private fun setOnClickListeners() {
        closeButton.setOnClickListener {
            dismiss()
        }
    }


}