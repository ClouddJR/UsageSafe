package com.clouddroid.usagesafe

import android.app.Application
import com.clouddroid.usagesafe.di.ApplicationComponent
import com.clouddroid.usagesafe.di.ApplicationModule
import com.clouddroid.usagesafe.di.DaggerApplicationComponent
import com.clouddroid.usagesafe.repositories.DatabaseRepository

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