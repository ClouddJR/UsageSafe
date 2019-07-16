package com.clouddroid.usagesafe.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.exporting.ExportActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setOnPreferenceClickListeners()
    }

    private fun setOnPreferenceClickListeners() {
        val exportPreference = findPreference<Preference>("export")
        exportPreference?.setOnPreferenceClickListener {
            context?.startActivity(Intent(context, ExportActivity::class.java))
            true
        }


    }
}