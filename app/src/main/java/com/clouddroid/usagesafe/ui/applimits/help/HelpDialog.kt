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

    private val focusHelpText =
        "When focus mode is enabled, all selected applications will be blocked regardless of whether there is a limit specified for them or not. It is applicable for study sessions or anytime you want to disconnect for a specific period od time.\nThis mode can be quickly enabled or disabled via a widget or a tile in notification bar"
    private val appLimitsHelpText =
        "Here you can specify daily limits for each application independently. Limit is reset at an hour specified in settings (beginning of the day). When you exceed a limit for a specific app in a given day, you won't be able to open this app for the rest of that day.\nIn order to edit or delete a limit you have to wait 15 seconds. This is supposed to discourage you from quickly editing or deleting a limit after exceeding it."

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

        titleText.text = when(helpForm) {
            HelpForm.FOCUS -> "Focus Mode"
            HelpForm.APP_LIMITS -> "App limits"
        }
    }

    private fun setOnClickListeners() {
        closeButton.setOnClickListener {
            dismiss()
        }
    }


}