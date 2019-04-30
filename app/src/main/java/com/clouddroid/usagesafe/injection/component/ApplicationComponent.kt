package com.clouddroid.usagesafe.injection.component

import com.clouddroid.usagesafe.UsageSafeApp
import com.clouddroid.usagesafe.injection.module.ApplicationModule
import com.clouddroid.usagesafe.injection.module.ViewModelsModule
import com.clouddroid.usagesafe.ui.base.BaseActivity
import com.clouddroid.usagesafe.ui.base.BaseDialogFragment
import com.clouddroid.usagesafe.ui.base.BaseFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelsModule::class])
interface ApplicationComponent {
    fun inject(target: UsageSafeApp)
    fun inject(target: BaseActivity)
    fun inject(target: BaseFragment)
    fun inject(target: BaseDialogFragment)
}