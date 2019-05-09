package com.clouddroid.usagesafe.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.clouddroid.usagesafe.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}