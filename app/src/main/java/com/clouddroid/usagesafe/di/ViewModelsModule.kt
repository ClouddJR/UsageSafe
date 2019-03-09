package com.clouddroid.usagesafe.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clouddroid.usagesafe.viewmodels.TodaysStatsViewModel
import com.clouddroid.usagesafe.viewmodels.ViewModelFactory
import com.clouddroid.usagesafe.viewmodels.ViewModelKey
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
}