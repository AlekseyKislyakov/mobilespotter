package com.example.mobile_spotter.injection

import android.app.Application
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferences(application: Application): PreferencesStorage {
        return PreferencesStorage(application)
    }

}