package com.clouddroid.usagesafe.injection.module

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.clouddroid.usagesafe.data.local.*
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.util.PreferencesUtils.defaultPrefs
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences = defaultPrefs(application)

    @Provides
    @Singleton
    fun provideUsageStatsManager(): UsageStatsManager =
        ContextCompat.getSystemService(application, UsageStatsManager::class.java)!!

    @Provides
    @Singleton
    fun providePackageManager(): PackageManager = application.packageManager

    @Provides
    @Singleton
    fun provideAppLimitsDao(): AppLimitDao = LocalDatabase.getInstance(application).appLimitDao()

    @Provides
    @Singleton
    fun provideLogEventsDao(): LogEventDao = LocalDatabase.getInstance(application).logEventDao()

    @Provides
    @Singleton
    fun provideGroupLimitsDao(): GroupLimitDao = LocalDatabase.getInstance(application).groupLimitDao()

    @Provides
    @Singleton
    fun provideFocusModeAppsDao(): FocusModeAppDao = LocalDatabase.getInstance(application).focusModeAppDao()

    @Provides
    @Singleton
    fun provideDatabaseRepository(
        appLimitsDataSource: AppLimitDao,
        logEventsDataSource: LogEventDao,
        groupLimitsDataSource: GroupLimitDao,
        focusModeAppsDataSource: FocusModeAppDao
    ): DatabaseRepository =
        DatabaseRepository(appLimitsDataSource, logEventsDataSource, groupLimitsDataSource, focusModeAppsDataSource)

}