package com.clouddroid.usagesafe.ui.applimits

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.util.PackageInfoUtils
import kotlinx.android.synthetic.main.dialog_app_limit_edit.*
import java.util.concurrent.TimeUnit

class AppLimitEditDialog(
    private val passedContext: Context,
    private val appLimit: AppLimit,
    private val databaseRepository: DatabaseRepository
) : Dialog(passedContext) {

    private var counter = 15
    private val counterHandler = Handler()
    private val counterUpdateRunnable = object : Runnable {
        override fun run() {
            val buttonTitle = "Delete"
            if (counter >= 0) {
                deleteLimitBT.text = "$buttonTitle (${counter--})"
                counterHandler.postDelayed(this, 1000)
            } else {
                deleteLimitBT.text = "$buttonTitle"
                deleteLimitBT.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_app_limit_edit)
        setLogoAndAppName()
        startTimer()
        setUpNumberPickers()
        setUpOnClickListeners()
    }

    private fun setLogoAndAppName() {
        Glide.with(passedContext).load(PackageInfoUtils.getRawAppIcon(appLimit.packageName, passedContext))
            .into(appIconIV)
        appNameTV.text = PackageInfoUtils.getAppName(appLimit.packageName, passedContext)
    }

    private fun startTimer() {
        deleteLimitBT.isEnabled = false
        counterHandler.post(counterUpdateRunnable)
    }

    private fun setUpNumberPickers() {
        var minutes = TimeUnit.MILLISECONDS.toMinutes(appLimit.limit)
        val hours = minutes / 60
        minutes %= 60

        hourNumberPicker.minValue = 0
        hourNumberPicker.maxValue = 23

        minuteNumberPicker.minValue = 0
        minuteNumberPicker.maxValue = 59

        hourNumberPicker.value = hours.toInt()
        minuteNumberPicker.value = minutes.toInt()
    }

    private fun setUpOnClickListeners() {
        deleteLimitBT.setOnClickListener {
            val appLimitToBeDeleted = AppLimit()
            appLimitToBeDeleted.packageName = appLimit.packageName
            appLimitToBeDeleted.limit =
                getLimitFromHoursAndMinutes(hourNumberPicker.value, minuteNumberPicker.value)
            databaseRepository.deleteAppLimit(appLimitToBeDeleted)
            dismiss()
        }

        updateLimitBT.setOnClickListener {
            val appLimitToBeUpdated = AppLimit()
            appLimitToBeUpdated.packageName = appLimit.packageName
            appLimitToBeUpdated.limit =
                getLimitFromHoursAndMinutes(hourNumberPicker.value, minuteNumberPicker.value)
            databaseRepository.addAppLimit(appLimitToBeUpdated)
            dismiss()
        }
    }

    private fun getLimitFromHoursAndMinutes(hours: Int?, minutes: Int?): Long {
        val hoursMillis = TimeUnit.HOURS.toMillis(hours?.toLong() ?: 0)
        val minutesMillis = TimeUnit.MINUTES.toMillis(minutes?.toLong() ?: 0)
        return hoursMillis + minutesMillis
    }
}