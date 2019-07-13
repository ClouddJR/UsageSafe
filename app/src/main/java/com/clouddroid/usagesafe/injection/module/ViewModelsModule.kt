package com.clouddroid.usagesafe.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsViewModel
import com.clouddroid.usagesafe.ui.applimits.AppLimitsViewModel
import com.clouddroid.usagesafe.ui.applimits.adddialog.AppLimitsDialogViewModel
import com.clouddroid.usagesafe.ui.applimits.focus.FocusAppListViewModel
import com.clouddroid.usagesafe.ui.base.ViewModelFactory
import com.clouddroid.usagesafe.ui.base.ViewModelKey
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsViewModel
import com.clouddroid.usagesafe.ui.exporting.ExportActivityViewModel
import com.clouddroid.usagesafe.ui.historystats.HistoryStatsViewModel
import com.clouddroid.usagesafe.ui.historystats.applaunches.AppLaunchesViewModel
import com.clouddroid.usagesafe.ui.historystats.screen.ScreenTimeViewModel
import com.clouddroid.usagesafe.ui.historystats.unlocks.UnlocksViewModel
import com.clouddroid.usagesafe.ui.main.MainActivityViewModel
import com.clouddroid.usagesafe.ui.todaystats.TodaysStatsViewModel
import com.clouddroid.usagesafe.ui.welcome.PermissionActivityViewModel
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
    @ViewModelKey(HistoryStatsViewModel::class)
    fun provideHistoryStatsViewModel(viewModel: HistoryStatsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(ScreenTimeViewModel::class)
    fun provideScreenTimeViewModel(viewModel: ScreenTimeViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(UnlocksViewModel::class)
    fun provideUnlocksViewModel(viewModel: UnlocksViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(AppLaunchesViewModel::class)
    fun provideAppLaunchesViewModel(viewModel: AppLaunchesViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(AppDetailsViewModel::class)
    fun provideAppDetailsViewModel(viewModel: AppDetailsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(DayDetailsViewModel::class)
    fun provideDayDetailsViewModel(viewModel: DayDetailsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(AppLimitsDialogViewModel::class)
    fun provideAppLimitsDialogViewModel(viewModel: AppLimitsDialogViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(FocusAppListViewModel::class)
    fun provideFocusAppListViewModel(viewModel: FocusAppListViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(ExportActivityViewModel::class)
    fun provideExportActivityViewModel(viewModel: ExportActivityViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(PermissionActivityViewModel::class)
    fun providePermissionActivityViewModel(viewModel: PermissionActivityViewModel): ViewModel = viewModel

}