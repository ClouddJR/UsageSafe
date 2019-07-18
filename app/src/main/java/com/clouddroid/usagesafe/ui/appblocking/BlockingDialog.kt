package com.clouddroid.usagesafe.ui.appblocking

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import com.clouddroid.usagesafe.R
import kotlinx.android.synthetic.main.dialog_app_block.*

class BlockingDialog(
    private val mode: BlockingMode,
    private val passedContext: Context,
    private val onButtonClick: () -> Unit
) :
    Dialog(passedContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_app_block)
        setCanceledOnTouchOutside(false)
        setOnClickListener()
        setBlockingText()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
    }

    private fun setOnClickListener() {
        exitButton.setOnClickListener {
            onButtonClick.invoke()
            dismiss()
        }
    }

    private fun setBlockingText() {
        blockingText.text = when (mode) {
            BlockingMode.APP_LIMIT -> passedContext.getString(R.string.dialog_blocking_limit_reached)
            BlockingMode.FOCUS_MODE -> passedContext.getString(R.string.dialog_blocking_focus_mode)
        }
    }
}