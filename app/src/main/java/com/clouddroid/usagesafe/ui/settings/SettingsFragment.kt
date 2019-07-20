package com.clouddroid.usagesafe.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.exporting.ExportActivity
import com.clouddroid.usagesafe.ui.premium.PremiumActivity
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.email
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

        val contactPreference = findPreference<Preference>("contact")
        contactPreference?.setOnPreferenceClickListener {
            email("usagesafe@gmail.com", "Usage Safe app")
        }

        val privacyPolicyPreference = findPreference<Preference>("privacy_policy")
        privacyPolicyPreference?.setOnPreferenceClickListener {
            browse("https://sites.google.com/view/usprivacypolicy")
        }

        val ratePreference = findPreference<Preference>("rate")
        ratePreference?.setOnPreferenceClickListener {
            val appPackageName = context?.packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (exception: android.content.ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
            true
        }
    }
}