package com.clouddroid.usagesafe.di

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import com.clouddroid.usagesafe.utils.PreferencesUtils.defaultPrefs
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
    fun provideDatabaseRepository(): DatabaseRepository = DatabaseRepository()

}