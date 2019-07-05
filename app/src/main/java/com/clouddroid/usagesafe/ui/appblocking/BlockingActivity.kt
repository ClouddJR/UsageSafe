package com.clouddroid.usagesafe.ui.appblocking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BlockingActivity : AppCompatActivity() {

    companion object {
        const val BLOCKING_MODE_KEY = "blocking_mode"
    }

    private lateinit var dialog: BlockingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getSerializableExtra(BLOCKING_MODE_KEY) as BlockingMode
        dialog = BlockingDialog(mode, this) {
            Intent(Intent.ACTION_MAIN).also { intent ->
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
    }

    override fun onBackPressed() {}
}