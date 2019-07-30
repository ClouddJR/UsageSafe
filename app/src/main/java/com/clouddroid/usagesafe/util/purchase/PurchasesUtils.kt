package com.clouddroid.usagesafe.util.purchase

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.premium.PremiumActivity
import com.clouddroid.usagesafe.util.PreferencesKeys
import com.clouddroid.usagesafe.util.PreferencesUtils.defaultPrefs
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.PreferencesUtils.set
import kotlinx.android.synthetic.main.dialog_premium_request.*
import org.jetbrains.anko.startActivity

object PurchasesUtils {

    const val base64EncodedPublicKey =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhz2ngqj8tLcqk1+sAyx1KaM3UraV7KPNz16hV8xHZGDYao83F0yfYjl1+6ZeYBztlCpN2dPis624Pb5AXQ29ESfV5P2FCYKQVkRoZj2XvT+h47yqDsaKdcFyUYF9TZ6oxPmEyCkwhwYZT72TSWOyPspWmzi/8AJOgXf7lnyj26R5L1+BgvzR3IiSiWOrk1U5LrbjqzDm0/cJrMIvAIrdnsK44/JnX0MuioubhMO7HtcWxTzA+XtwiTLGsww+vcdQNmMg1Hrvx/cp3tewXhx3YIgnuYCw3zIvNgz5GQAAAQOcUelLC0Yv3LwOIYRq4l1+NFJ7zHyY04dkWRtidJ6DgQIDAQAB"
    lateinit var sharedPreferences: SharedPreferences

    object GotInventoryListener : IabHelper.QueryInventoryFinishedListener {
        override fun onQueryInventoryFinished(result: IabResult?, inv: Inventory?) {
            if (result?.isFailure != false) {
                //nothing
            } else {
                val purchasePro = inv?.getPurchase(PurchaseIDs.PURCHASE_REGULAR)
                if (purchasePro != null) {
                    sharedPreferences[PreferencesKeys.PREF_IS_PREMIUM_USER] = true
                }

                val purchaseProBig = inv?.getPurchase(PurchaseIDs.PURCHASE_EXTENDED)
                if (purchaseProBig != null) {
                    sharedPreferences[PreferencesKeys.PREF_IS_PREMIUM_USER] = true
                }
            }
        }
    }

    fun displayPremiumInfoDialog(context: Context?) {
        InformationDialog(context!!).show()
    }

    fun isPremiumUser(context: Context?): Boolean {
        context?.let {
            val prefs = defaultPrefs(context)
            return prefs[PreferencesKeys.PREF_IS_PREMIUM_USER] ?: false
        }

        return false
    }

    class InformationDialog(context: Context) : Dialog(context) {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_premium_request)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            showMoreBT.setOnClickListener {
                context.startActivity<PremiumActivity>()
                dismiss()
            }

            closeButton.setOnClickListener {
                dismiss()
            }
        }
    }
}