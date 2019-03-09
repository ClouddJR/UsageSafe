package com.clouddroid.usagesafe

import android.app.Application
import com.clouddroid.usagesafe.di.ApplicationComponent
import com.clouddroid.usagesafe.di.ApplicationModule
import com.clouddroid.usagesafe.di.DaggerApplicationComponent

class UsageSafeApp : Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }
}