package com.clouddroid.usagesafe

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.injection.component.ApplicationComponent
import com.clouddroid.usagesafe.injection.component.DaggerApplicationComponent
import com.clouddroid.usagesafe.injection.module.ApplicationModule
import com.clouddroid.usagesafe.util.NotificationUtils.CHANNEL_ID

class UsageSafeApp : Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
        initRealm()
        createNotificationChannel()
    }

    private fun injectDependencies() {
        component.inject(this)
    }

    private fun initRealm() {
        DatabaseRepository.RealmInitializer.initRealm(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = "App limits"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.setShowBadge(false)
            channel.enableVibration(false)
            channel.enableLights(false)

            notificationManager.createNotificationChannel(channel)
        }
    }
}