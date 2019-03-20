package com.clouddroid.usagesafe.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clouddroid.usagesafe.viewmodels.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ViewModelsModule {

    @Provides
    fun providesViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory = factory

    @Provides
    @IntoMap
    @ViewModelKey(TodaysStatsViewModel::class)
    fun provideTodaysStatsViewModel(viewModel: TodaysStatsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(AppLimitsViewModel::class)
    fun provideAppLimitsViewModel(viewModel: AppLimitsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun provideMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    fun provideContactsViewModel(viewModel: ContactsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(HistoryStatsViewModel::class)
    fun provideHistoryStatsViewModel(viewModel: HistoryStatsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(ScreenTimeViewModel::class)
    fun provideScreenTimeViewModel(viewModel: ScreenTimeViewModel): ViewModel = viewModel
}