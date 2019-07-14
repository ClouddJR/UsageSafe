package com.clouddroid.usagesafe.data.service

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.clouddroid.usagesafe.UsageSafeApp
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.util.PreferencesKeys
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.PreferencesUtils.set
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@TargetApi(Build.VERSION_CODES.N)
class QuickSettingsService : TileService() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    override fun onCreate() {
        super.onCreate()
        (application as UsageSafeApp).component.inject(this)
    }

    override fun onStartListening() {
        qsTile.state = getTileStatusBasedOnFocusMode()
        qsTile.updateTile()
    }

    override fun onClick() {
        val state = getTileStatusBasedOnFocusMode()

        qsTile.state = when (state) {
            Tile.STATE_ACTIVE -> Tile.STATE_INACTIVE
            Tile.STATE_INACTIVE -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }

        updateFocusModeStateInPrefs(qsTile.state)
        toggleAppLimitsService(qsTile.state == Tile.STATE_ACTIVE)
        qsTile.updateTile()
    }

    private fun getTileStatusBasedOnFocusMode(): Int {
        return when (sharedPrefs[PreferencesKeys.PREF_IS_FOCUS_MODE_ENABLED, false]) {
            true -> Tile.STATE_ACTIVE
            false -> Tile.STATE_INACTIVE
            null -> Tile.STATE_INACTIVE
        }
    }

    private fun updateFocusModeStateInPrefs(state: Int) {
        sharedPrefs[PreferencesKeys.PREF_IS_FOCUS_MODE_ENABLED] = when (state) {
            Tile.STATE_INACTIVE -> false
            Tile.STATE_ACTIVE -> true
            else -> false
        }
    }

    @SuppressLint("CheckResult")
    private fun toggleAppLimitsService(isEnabled: Boolean) {
        databaseRepository.getNumberOfAppLimits()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { numberOfLimits ->
                //if there are no app limits saved in db and focus mode is disabled, stop the service
                if (numberOfLimits == 0 && !isEnabled) {
                    stopUsageMonitorService()
                } else {
                    startUsageMonitorService(isEnabled)
                }
            }
    }

    private fun stopUsageMonitorService() {
        val intent = Intent(this, AppUsageMonitorService::class.java)
        stopService(intent)
    }

    private fun startUsageMonitorService(data: Boolean?) {
        val intent = Intent(this, AppUsageMonitorService::class.java)
        intent.putExtra(AppUsageMonitorService.FOCUS_MODE_KEY, data)
        ContextCompat.startForegroundService(this, intent)
    }
}