package com.clouddroid.usagesafe.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.main.MainActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsContainer, SettingsFragment())
            .commit()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return true
    }
}