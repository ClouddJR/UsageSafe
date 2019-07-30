package com.clouddroid.usagesafe.ui.premium

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.util.PreferencesKeys
import com.clouddroid.usagesafe.util.PreferencesUtils.defaultPrefs
import com.clouddroid.usagesafe.util.PreferencesUtils.set
import com.clouddroid.usagesafe.util.purchase.*
import kotlinx.android.synthetic.main.activity_premium.*
import kotlinx.android.synthetic.main.dialog_premium_information.*

class PremiumActivity : AppCompatActivity(), IabHelper.OnIabPurchaseFinishedListener {

    private lateinit var helper: IabHelper
    private var isHelperSetup = false
    private var isPremiumAlreadyBought = false
    private var isPremiumBigAlreadyBought = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        setUpHelper()
        setUpButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!::helper.isInitialized) return
        if (!helper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onIabPurchaseFinished(result: IabResult?, purchase: Purchase?) {
        result?.let {
            when {
                result.isFailure -> {
                    //nothing
                }
                purchase?.sku == PurchaseIDs.PURCHASE_REGULAR -> {
                    PurchasesUtils.sharedPreferences[PreferencesKeys.PREF_IS_PREMIUM_USER] = true
                }
                purchase?.sku == PurchaseIDs.PURCHASE_EXTENDED -> {
                    PurchasesUtils.sharedPreferences[PreferencesKeys.PREF_IS_PREMIUM_USER] = true
                }
            }
        }
    }

    private fun setUpHelper() {
        PurchasesUtils.sharedPreferences = defaultPrefs(this)
        helper = IabHelper(this, PurchasesUtils.base64EncodedPublicKey)
        helper.startSetup { result ->
            if (result.isSuccess) {
                isHelperSetup = true
                try {

                    //checking if a user have already bought any of the premium accounts
                    helper.queryInventoryAsync { res, inv ->
                        if (res?.isFailure != false) {
                            //nothing
                        } else {
                            val purchasePro = inv?.getPurchase(PurchaseIDs.PURCHASE_REGULAR)

                            if (purchasePro != null) {
                                isPremiumAlreadyBought = true
                            }
                            val purchaseProBig = inv?.getPurchase(PurchaseIDs.PURCHASE_EXTENDED)

                            if (purchaseProBig != null) {
                                isPremiumBigAlreadyBought = true
                            }
                        }
                    }
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setUpButtons() {
        buyButton.setOnClickListener {
            if (isHelperSetup) {

                if (!isPremiumAlreadyBought) {
                    try {
                        helper.launchPurchaseFlow(
                            this, PurchaseIDs.PURCHASE_REGULAR, 10001,
                            this, ""
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    InformationDialog(this).show()
                }
            }
        }

        buyButtonBig.setOnClickListener {
            if (isHelperSetup) {
                if (!isPremiumBigAlreadyBought) {
                    try {
                        helper.launchPurchaseFlow(
                            this, PurchaseIDs.PURCHASE_EXTENDED, 10001,
                            this, ""
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    InformationDialog(this).show()
                }
            }
        }
    }

    class InformationDialog(context: Context) : Dialog(context) {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_premium_information)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            closeButton.setOnClickListener {
                dismiss()
            }
        }
    }

}