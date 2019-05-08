package com.clouddroid.usagesafe.ui.appblocking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BlockingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialog = BlockingDialog(this) {
            Intent(Intent.ACTION_MAIN).also { intent ->
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        dialog.show()
    }

    override fun onBackPressed() {}
}