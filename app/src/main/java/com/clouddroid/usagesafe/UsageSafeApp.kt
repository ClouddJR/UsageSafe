package com.clouddroid.usagesafe

import android.app.Application
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.injection.component.ApplicationComponent
import com.clouddroid.usagesafe.injection.component.DaggerApplicationComponent
import com.clouddroid.usagesafe.injection.module.ApplicationModule

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
    }

    private fun initRealm() {
        DatabaseRepository.RealmInitializer.initRealm(this)
    }

    private fun injectDependencies() {
        component.inject(this)
    }
}