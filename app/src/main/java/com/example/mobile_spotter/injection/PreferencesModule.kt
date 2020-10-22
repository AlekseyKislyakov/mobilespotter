package com.example.mobile_spotter.injection

import android.app.Application
import android.content.Context
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.data.remote.ApiServiceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferences(application: Application) : PreferencesStorage {
        return PreferencesStorage(application)
    }

}