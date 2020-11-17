package com.example.mobile_spotter.injection

import android.app.Application
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.utils.SessionHandler
import com.example.mobile_spotter.utils.SessionHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object SessionHandlerModule {

    @Provides
    @Singleton
    fun provideSessionHandler(application: Application): SessionHandler {
        return SessionHandlerImpl
    }

}