package com.clouddroid.usagesafe.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.exporting.ExportActivity
import com.clouddroid.usagesafe.ui.premium.PremiumActivity
import org.jetbrains.anko.support.v4.startActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setOnPreferenceClickListeners()
    }

    private fun setOnPreferenceClickListeners() {
        val exportPreference = findPreference<Preference>("export")
        exportPreference?.setOnPreferenceClickListener {
            startActivity<ExportActivity>()
            true
        }

        val premiumPreference = findPreference<Preference>("premium")
        premiumPreference?.setOnPreferenceClickListener {
            startActivity<PremiumActivity>()
            true
        }
    }
}