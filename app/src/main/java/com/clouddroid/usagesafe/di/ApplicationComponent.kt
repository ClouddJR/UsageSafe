package com.clouddroid.usagesafe.di

import com.clouddroid.usagesafe.UsageSafeApp
import com.clouddroid.usagesafe.activities.BaseActivity
import com.clouddroid.usagesafe.fragments.BaseFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelsModule::class])
interface ApplicationComponent {
    fun inject(target: UsageSafeApp)
    fun inject(target: BaseActivity)
    fun inject(target: BaseFragment)
}