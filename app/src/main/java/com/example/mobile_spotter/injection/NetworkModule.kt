package com.example.mobile_spotter.injection

import android.app.Application
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.data.remote.ApiServiceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(application: Application): ApiService = ApiServiceFactory.newInstance(application)

}