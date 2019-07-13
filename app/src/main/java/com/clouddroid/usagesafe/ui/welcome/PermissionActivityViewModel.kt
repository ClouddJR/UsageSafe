package com.clouddroid.usagesafe.ui.welcome

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Process
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.ui.main.MainActivity
import javax.inject.Inject
import javax.inject.Named

class PermissionActivityViewModel @Inject constructor(
    private val appOpsManager: AppOpsManager,
    private val context: Context,
    @Named("packageName") private val packageName: String
) : ViewModel() {

    private lateinit var callback: AppOpsManager.OnOpChangedListener
    private var previousValue: Boolean? = null
    private val handler = Handler()

    fun startWatchingForPermissionChanges() {
        callback = AppOpsManager.OnOpChangedListener { _, _ ->
            //we are not on the main thread, so post this to the handler
            handler.post(redirectIfPermissionGranted)
        }
        appOpsManager.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS, packageName, callback)
    }

    private val redirectIfPermissionGranted = Runnable {
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
        )

        val granted = if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(AppOpsManager.OPSTR_GET_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }

        //each permission change triggers listener twice (no idea why), so ignore the second call
        if (previousValue == null || previousValue != granted) {
            previousValue = granted
            if (granted) {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        previousValue = null
        if (::callback.isInitialized) {
            appOpsManager.stopWatchingMode(callback)
        }
    }
}